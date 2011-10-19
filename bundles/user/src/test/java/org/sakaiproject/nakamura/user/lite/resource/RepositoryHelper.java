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
package org.sakaiproject.nakamura.user.lite.resource;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.lite.BaseMemoryRepository;

import java.io.IOException;

public class RepositoryHelper {

  public static Repository getRepository(String[] users, String groups[]) throws ClientPoolException, StorageClientException, AccessDeniedException, ClassNotFoundException, IOException {
    BaseMemoryRepository baseMemoryRepository = new BaseMemoryRepository();
    Session session = baseMemoryRepository.getRepository().loginAdministrative();
    AuthorizableManager authorizableManager = session.getAuthorizableManager();
    for ( String user : users ) {
      authorizableManager.createUser(user, user, "test", null);
    }
    for ( String group : groups ) {
      authorizableManager.createGroup(group,group, null);
    }
    session.logout();
    return baseMemoryRepository.getRepository();
  }
  
  

}
