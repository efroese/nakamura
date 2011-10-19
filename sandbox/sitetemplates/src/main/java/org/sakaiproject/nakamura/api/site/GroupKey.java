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
package org.sakaiproject.nakamura.api.site;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import org.apache.jackrabbit.api.security.user.Group;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

public class GroupKey extends AuthorizableKey {

  private Group group;
  private List<AuthorizableKey> children;

  public GroupKey(Group group) throws RepositoryException {
    super(group);
    this.group = group;
    this.children = new ArrayList<AuthorizableKey>();
  }

  public GroupKey(Group group, List<AuthorizableKey> children) throws RepositoryException {
    super(group);
    this.group = group;
    setChildren(children);
  }

  public Group getGroup() {
    return group;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.site.AuthorizableKey#equals(java.lang.Object)
   */
  @Override
  @SuppressWarnings(justification = "ID's are Unique, so Authorizable Equals is valid, as is hashcode ", value = { "HE_EQUALS_NO_HASHCODE" })
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  /**
   * @param children
   *          the children to set
   */
  public void setChildren(List<AuthorizableKey> children) {
    this.children = children;
  }

  /**
   * @return the children
   */
  public List<AuthorizableKey> getChildren() {
    return children;
  }

}
