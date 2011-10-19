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
package org.sakaiproject.nakamura.api.http.usercontent;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Server protection Veto implementations can prevent a resource from streaming regardless
 * of what the ServerProtectionService deduces. Implementing this interface is a last
 * resort for streaming servlets that dont expose normal request to url mapping semantics.
 */
public interface ServerProtectionVeto {

  /**
   * returns true if the implementation is going to veto the request.
   * @param srequest
   * @return
   */
  boolean willVeto(SlingHttpServletRequest srequest);

  /**
   * @param srequest
   * @return the vetoed decision on the request, true will stream, false will not.
   */
  boolean safeToStream(SlingHttpServletRequest srequest);

}
