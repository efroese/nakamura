package org.sakaiproject.nakamura.api.user.indexing;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.solr.common.SolrInputDocument;
import org.osgi.service.event.Event;
import org.sakaiproject.nakamura.api.solr.RepositorySession;

/**
 * User: Duffy Gillman <duffy@rsmart.com>
 * Date: Mar 16, 2012
 * Time: 9:43:40 AM
 */
public interface AuthorizableIndexingWorker
{
    public void decorateSolrInputDocument (SolrInputDocument indexDoc, Event osgiEvent, Authorizable authorizable,
                                           RepositorySession repositorySession)
        throws AuthorizableIndexingException;
}
