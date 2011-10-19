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
package org.sakaiproject.nakamura.memory;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.StorageCacheManager;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;

import java.util.Map;

@Component(immediate=true, metatype=true)
@Service(value=StorageCacheManager.class)
public class StorageCacheManagerImpl implements StorageCacheManager {

  private Map<String, CacheHolder> accessControlCache;
  private Map<String, CacheHolder> authorizableCache;
  private Map<String, CacheHolder> contentCache;
  
  @Reference
  private CacheManagerService cacheManagerService;

  @Activate
  public void activate(Map<String, Object> props) {
    Cache<CacheHolder> accesssControlCacheCache = cacheManagerService.getCache("accessControlCache", CacheScope.CLUSTERINVALIDATED);
    Cache<CacheHolder> authorizableCacheCache = cacheManagerService.getCache("authorizableCache", CacheScope.CLUSTERINVALIDATED);
    Cache<CacheHolder> contentCacheCache = cacheManagerService.getCache("contentCache", CacheScope.CLUSTERINVALIDATED);
    accessControlCache = new MapDeligate<String, CacheHolder>(accesssControlCacheCache);
    authorizableCache = new MapDeligate<String, CacheHolder>(authorizableCacheCache);
    contentCache = new MapDeligate<String, CacheHolder>(contentCacheCache);
  }
  
  @Deactivate
  public void deactivate(Map<String, Object> props) {
    
  }
  
  
  public Map<String, CacheHolder> getAccessControlCache() {
    return accessControlCache;
  }

  public Map<String, CacheHolder> getAuthorizableCache() {
    return authorizableCache;
  }

  public Map<String, CacheHolder> getContentCache() {
    return contentCache;
  }

}
