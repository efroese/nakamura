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
package org.sakaiproject.nakamura.http.usercontent;

import org.sakaiproject.nakamura.api.auth.trusted.TrustedTokenService;
import org.sakaiproject.nakamura.api.auth.trusted.TrustedTokenServiceWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

final class UserContentAuthenticationTokenServiceWrapper extends TrustedTokenServiceWrapper {

  public static final String TYPE = "U";

  UserContentAuthenticationTokenServiceWrapper(
      UserContentAuthenticationHandler userContentAuthenticationHandler,
      TrustedTokenService delegate) {
    super(validate(userContentAuthenticationHandler,delegate));
  }

  private static TrustedTokenService validate(
      UserContentAuthenticationHandler userContentAuthenticationHandler,
      TrustedTokenService delegate) {
    if ( !UserContentAuthenticationHandler.class.equals(userContentAuthenticationHandler.getClass()) ) {
      throw new IllegalArgumentException("Invalid use of UserContentAuthenticationTokenServiceWrapper");
    }
    return delegate;
  }

  public void addToken(HttpServletRequest request, HttpServletResponse response) {
    injectToken(request, response);
  }

  @Override
  public String getType() {
    return TYPE;
  }

}
