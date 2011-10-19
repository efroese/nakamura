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
import org.apache.sling.api.SlingHttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Protects the server from requests that should not be trusted. If you write a servlet
 * that gets users content raw, the it should check if the request is safe
 * (isRequestSafe()) before processing the request.
 */
public interface ServerProtectionService {

  /**
   * Check if the request is safe. If not safe the response will have been setup with a
   * suitable redirect URL to make the request safe.
   * 
   * @param srequest
   * @param sresponse
   * @return true if the request is safe for further processing in this context, false if
   *         not. If false, the request should be committed imediately with no further
   *         processing.
   * @throws UnsupportedEncodingException
   * @throws IOException
   */
  boolean isRequestSafe(SlingHttpServletRequest srequest,
      SlingHttpServletResponse sresponse) throws UnsupportedEncodingException,
      IOException;

  /**
   * Check if the method is safe in this context, if its not safe in this context, then
   * the response should be committed immediately with no further processing.
   * 
   * @param hrequest
   * @param hresponse
   * @return true if safe, false if not.
   * @throws IOException
   */
  boolean isMethodSafe(HttpServletRequest hrequest, HttpServletResponse hresponse)
      throws IOException;

  /**
   * @param request
   * @return the UserID associated with the request transfered from annother domain or
   *         Anon if the user could not be identified, was not trusted or any other
   *         problems.
   */
  String getTransferUserId(HttpServletRequest request);

  /**
   * @param hrequest
   * @return true if the host is trusted.
   */
  boolean isSafeHost(HttpServletRequest hrequest);

}
