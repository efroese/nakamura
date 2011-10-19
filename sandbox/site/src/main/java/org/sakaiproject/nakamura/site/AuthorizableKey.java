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
package org.sakaiproject.nakamura.site;

import org.apache.jackrabbit.api.security.user.Authorizable;

import javax.jcr.RepositoryException;

public class AuthorizableKey {

  private String id;
  private Authorizable authorizable;
  private String firstName = "";
  private String lastName = "";

  public AuthorizableKey(Authorizable authorizable) throws RepositoryException {
    this.id = authorizable.getID();
    this.authorizable = authorizable;
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof AuthorizableKey))
      return false;
    return ((AuthorizableKey)obj).getID().equals(getID());
  }

  private String getID() {
    return id;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public Authorizable getAuthorizable() {
    return authorizable;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLastName() {
    return lastName;
  }
}
