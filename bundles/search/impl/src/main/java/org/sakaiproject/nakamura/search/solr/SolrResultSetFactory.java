/**
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.search.solr;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.search.SearchUtil;
import org.sakaiproject.nakamura.api.search.solr.Query;
import org.sakaiproject.nakamura.api.search.solr.ResultSetFactory;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchException;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchResultSet;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchUtil;
import org.sakaiproject.nakamura.api.solr.SolrServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.cluster.ClusterTrackingService;
import org.sakaiproject.nakamura.api.cluster.ClusterServer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
@Component(metatype = true)
@Service
@Properties(value = {
    @Property(name = "type", value = Query.SOLR),
    @Property(name = "event.topics", value = {
        "org/sakaiproject/nakamura/lite/content/DELETE",
        "org/sakaiproject/nakamura/solr/COMMIT"})})

  public class SolrResultSetFactory implements ResultSetFactory, EventHandler {
  @Property(longValue = 100L)
  private static final String VERY_SLOW_QUERY_TIME = "verySlowQueryTime";
  @Property(longValue = 10L)
  private static final String SLOW_QUERY_TIME = "slowQueryTime";
  @Property(intValue = 100)
  private static final String DEFAULT_MAX_RESULTS = "defaultMaxResults";

  private static final String DELETED_PATH_CACHE = "deletedPathQueue";

  /** only used to mark the logger */
  private final class SlowQueryLogger { }

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrResultSetFactory.class);
  private static final Logger SLOW_QUERY_LOGGER = LoggerFactory.getLogger(SlowQueryLogger.class);

  @Reference
  private SolrServerService solrSearchService;

  @Reference
  CacheManagerService cacheManagerService;

  @Reference
  ClusterTrackingService clusterTrackingService;

  private int defaultMaxResults = 100; // set to 100 to allow testing
  private long slowQueryThreshold;
  private long verySlowQueryThreshold;

  @Activate
  protected void activate(Map<?, ?> props) {
    defaultMaxResults = OsgiUtil.toInteger(props.get(DEFAULT_MAX_RESULTS),
        defaultMaxResults);
    slowQueryThreshold = OsgiUtil.toLong(props.get(SLOW_QUERY_TIME), 10L);
    verySlowQueryThreshold = OsgiUtil.toLong(props.get(VERY_SLOW_QUERY_TIME), 100L);
  }


  /*
   * Get an instance of the cache used to track paths that have been marked as
   * deleted since the last Solr commit.  This cache is shared by all nodes in a
   * cluster, acting as a sort of shared memory.
   */
  private Cache<Object> getDeletedPathCache() {
    return cacheManagerService.getCache(DELETED_PATH_CACHE, CacheScope.CLUSTERREPLICATED);
  }


  /*
   * Record a path as having been deleted, preventing it from appearing in search results.
   *
   * @param path the path that was deleted
   */
  private synchronized void storeDeletedPath(String path) {
    Cache<Object> cache = getDeletedPathCache();
    String myId = clusterTrackingService.getCurrentServerId();

    Integer pathCount = (Integer)cache.get("pathCount@" + myId);
    pathCount = (pathCount == null) ? 0 : pathCount;

    cache.put("path[" + pathCount + "]@" + myId,
              SearchUtil.escapeString(path, Query.SOLR));
    cache.put("pathCount@" + myId, pathCount + 1);
  }


  /*
   * Clear the list of deleted nodes for this node.
   */
  private synchronized void clearDeletedPaths() {
    Cache<Object> cache = getDeletedPathCache();
    String myId = clusterTrackingService.getCurrentServerId();

    Integer pathCount = (Integer)cache.get("pathCount@" + myId);

    for (int idx = 0; pathCount != null && idx < pathCount; idx++) {
      cache.remove("path[" + idx + "]@" + myId);
    }

    cache.put("pathCount@" + myId, 0);
  }


  /**
   * Get a list of the paths that were deleted since the last Solr commit across all nodes
   * in the cluster. Escapes the paths to make sure they are safe for consumption in a
   * query.
   */
  private List<String> getDeletedPaths() {
    List<String> deletedPaths = new ArrayList<String>();
    Cache<Object> cache = getDeletedPathCache();

    for (ClusterServer server : clusterTrackingService.getAllServers()) {
      String serverId = server.getServerId();
      Integer pathCount = (Integer)cache.get("pathCount@" + serverId);

      for (int idx = 0; pathCount != null && idx < pathCount; idx++) {
        String path = (String)cache.get("path[" + idx + "]@" + serverId);

        if (path != null) {
          deletedPaths.add(SearchUtil.escapeString(path, Query.SOLR));
        }
      }
    }

    return deletedPaths;
  }


  public void handleEvent(Event event) {
    String topic = event.getTopic();

    if (topic.equals("org/sakaiproject/nakamura/lite/content/DELETE")) {
      String path = (String)event.getProperty("path");

      if (path != null) {
        storeDeletedPath(path);
      }
    } else if (topic.equals("org/sakaiproject/nakamura/solr/COMMIT")) {
      clearDeletedPaths();
    }
  }
      


  /**
   * Process a query string to search using Solr.
   *
   * @param request
   * @param query
   * @param asAnon
   * @param rs
   * @return
   * @throws SolrSearchException
   */
  @SuppressWarnings("rawtypes")
  public SolrSearchResultSet processQuery(SlingHttpServletRequest request, Query query,
      boolean asAnon) throws SolrSearchException {
    try {
      // Add reader restrictions to solr fq (filter query) parameter,
      // to prevent "reader restrictions" from affecting the solr score
      // of a document.
      Map<String, Object> originalQueryOptions = query.getOptions();
      Map<String, Object> queryOptions = Maps.newHashMap();
      Object filterQuery = null;

      if (originalQueryOptions != null) {
        // copy from originalQueryOptions in case its backed by a ImmutableMap,
        // which prevents saving of filter query changes.
        queryOptions.putAll(originalQueryOptions);
        if (queryOptions.get(CommonParams.FQ) != null) {
          filterQuery = queryOptions.get(CommonParams.FQ);
        }
      }

      Set<String> filterQueries = Sets.newHashSet();
      // add any existing filter queries to the set
      if (filterQuery != null) {
        if (filterQuery instanceof Object[]) {
          CollectionUtils.addAll(filterQueries, (Object[]) filterQuery);
        } else if (filterQuery instanceof Iterable) {
          CollectionUtils.addAll(filterQueries, ((Iterable) filterQuery).iterator());
        } else {
          filterQueries.add(String.valueOf(filterQuery));
        }
      }

      // apply readers restrictions.
      if (asAnon) {
        filterQueries.add("readers:" + User.ANON_USER);
      } else {
        Session session = StorageClientUtils.adaptToSession(request.getResourceResolver().adaptTo(javax.jcr.Session.class));
        if (!User.ADMIN_USER.equals(session.getUserId())) {
          AuthorizableManager am = session.getAuthorizableManager();
          Authorizable user = am.findAuthorizable(session.getUserId());
          Set<String> readers = Sets.newHashSet();
          for (Iterator<Group> gi = user.memberOf(am); gi.hasNext();) {
            readers.add(SearchUtil.escapeString(gi.next().getId(), Query.SOLR));
          }
          readers.add(SearchUtil.escapeString(session.getUserId(), Query.SOLR));
          filterQueries.add("readers:(" + StringUtils.join(readers," OR ") + ")");
        }
      }

      List<String> deletedPaths = getDeletedPaths();
      if (!deletedPaths.isEmpty()) {
        // these are escaped as they are collected
        filterQueries.add("-path:(" + StringUtils.join(deletedPaths, " OR ") + ")");
      }
      // save filterQuery changes
      queryOptions.put(CommonParams.FQ, filterQueries);

      SolrQuery solrQuery = buildQuery(request, query.getQueryString(), queryOptions);

      SolrServer solrServer = solrSearchService.getServer();
      if ( LOGGER.isDebugEnabled()) {
        try {
          LOGGER.debug("Performing Query {} ", URLDecoder.decode(solrQuery.toString(),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
      }
      long tquery = System.currentTimeMillis();
      QueryResponse response = solrServer.query(solrQuery);
      tquery = System.currentTimeMillis() - tquery;
      try {
        if ( tquery > verySlowQueryThreshold ) {
          SLOW_QUERY_LOGGER.error("Very slow solr query {} ms {} ",tquery, URLDecoder.decode(solrQuery.toString(),"UTF-8"));
        } else if ( tquery > slowQueryThreshold ) {
          SLOW_QUERY_LOGGER.warn("Slow solr query {} ms {} ",tquery, URLDecoder.decode(solrQuery.toString(),"UTF-8"));
        }
      } catch (UnsupportedEncodingException e) {
      }
      SolrSearchResultSetImpl rs = new SolrSearchResultSetImpl(response);
      if ( LOGGER.isDebugEnabled()) {
        LOGGER.debug("Got {} hits in {} ms", rs.getSize(), response.getElapsedTime());
      }
      return rs;
    } catch (StorageClientException e) {
      throw new SolrSearchException(500, e.getMessage());
    } catch (AccessDeniedException e) {
      throw new SolrSearchException(500, e.getMessage());
    } catch (SolrServerException e) {
        throw new SolrSearchException(500, e.getMessage());
    }
  }

  /**
   * @param request
   * @param query
   * @param queryString
   * @return
   */
  @SuppressWarnings("unchecked")
  private SolrQuery buildQuery(SlingHttpServletRequest request, String queryString,
      Map<String, Object> options) {
    // build the query
    SolrQuery solrQuery = new SolrQuery(queryString);
    long[] ranges = SolrSearchUtil.getOffsetAndSize(request, options);
    solrQuery.setStart((int) ranges[0]);
    solrQuery.setRows((int) ranges[1]);

    // add in some options
    if (options != null) {
      for (Entry<String, Object> option : options.entrySet()) {
        String key = option.getKey();
        Object val = option.getValue();
        if (CommonParams.SORT.equals(key)) {
          parseSort(solrQuery, String.valueOf(val));
        } else if (val instanceof Object[]) {
          for (Object v : (Object[]) val) {
            solrQuery.add(key, String.valueOf(v));
          }
        } else if (val instanceof Iterable) {
          for (Object v : (Iterable<Object>) val) {
            solrQuery.add(key, String.valueOf(v));
          }
        } else {
          solrQuery.add(key, String.valueOf(val));
        }
      }
    }
    return solrQuery;
  }

  /**
   * @param options
   * @param solrQuery
   * @param val
   */
  private void parseSort(SolrQuery solrQuery, String val) {
    /* disable KERN-1855 for now; needs more discussion. */
    // final String[] sortFields = solrQuery.getSortFields();
    // we were using setSortField, now using addSortField; verify state
    // if (sortFields != null && sortFields.length > 0) {
    // throw new IllegalStateException("Expected zero sort fields, found: " + sortFields);
    // }
    // final String[] criteria = val.split(",");
    // for (final String criterion : criteria) {
    // final String[] sort = StringUtils.split(criterion);
    final String[] sort = StringUtils.split(val);

    // use the *_sort fields to have predictable sorting.
    // many of the fields in the index have a lot of processing which
    // causes sorting to yield unpredictable results.
    String sortOn = ("score".equals(sort[0])) ? sort[0] : sort[0] + "_sort";
    switch (sort.length) {
    case 1:
      // solrQuery.addSortField(sort[0], ORDER.asc);
      solrQuery.setSortField(sortOn, ORDER.asc);
      break;
    case 2:
      String sortOrder = sort[1].toLowerCase();
      ORDER o = ORDER.asc;
      try {
        o = ORDER.valueOf(sortOrder);
      } catch (IllegalArgumentException a) {
        if (sortOrder.startsWith("d")) {
          o = ORDER.desc;
        } else {
          o = ORDER.asc;
        }
      }
      // solrQuery.addSortField(sort[0], o);
      solrQuery.setSortField(sortOn, o);
      break;
    default:
      LOGGER.warn("Expected the sort option to be 1 or 2 terms. Found: {}", val);
    }
    // }
  }
}
