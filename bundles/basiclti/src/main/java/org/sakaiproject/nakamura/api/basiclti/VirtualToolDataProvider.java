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
package org.sakaiproject.nakamura.api.basiclti;

import java.util.List;
import java.util.Map;

/**
 * Provides values to the BasicLTIConsumerServlet for virtual tool ids (i.e.
 * lti_vitual_tool_id).
 */
public interface VirtualToolDataProvider {

  /**
   * Provides values to the BasicLTIConsumerServlet for virtual tool ids (i.e.
   * lti_vitual_tool_id).
   * 
   * @param virtualToolId
   * @return null if this provider has no data for lti_vitual_tool_id.
   */
  Map<String, Object> getLaunchValues(String virtualToolId);

  /**
   * Returns only the key and secret. Should only be used for launch data or admin reads
   * of properties (i.e. normal users should never be able to read these values; however
   * that access control logic is applied external from this class).
   * 
   * @param virtualToolId
   * @return null if this provider has no data for lti_vitual_tool_id.
   */
  Map<String, Object> getKeySecret(String virtualToolId);

  /**
   * The list of virtual tools ids supported by this provider.
   * 
   * @return
   */
  List<String> getSupportedVirtualToolIds();
}
