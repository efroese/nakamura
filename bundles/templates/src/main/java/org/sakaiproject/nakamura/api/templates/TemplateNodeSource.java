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
package org.sakaiproject.nakamura.api.templates;

import javax.jcr.Node;

/**
 * Created by IntelliJ IDEA. User: zach Date: 1/5/11 Time: 11:08 AM To change this
 * template use File | Settings | File Templates.
 */
public interface TemplateNodeSource {
  /**
   * The resource Source implementation to be used by the resource loader, set to an
   * implementation of ReourceSource.
   */
  public static final String JCR_RESOURCE_LOADER_RESOURCE_SOURCE = "resourceSource";

  /**
   * @return gets the resource for the current context.
   */
  Node getNode();
}
