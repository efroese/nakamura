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
package org.sakaiproject.nakamura.jetty;

import org.apache.felix.http.api.ExtHttpService;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This class that operates as a managed service.
 */
public class OSGiGZipFilter extends GzipFilter {

  protected ExtHttpService extHttpService;

  @SuppressWarnings("rawtypes")
  public void activate(Map<String, Object> properties) throws ServletException {
    Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.putAll(properties);
    extHttpService.registerFilter(this, ".*", (Dictionary) properties, 100, null);

  }

  @Override
  public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
      throws IOException, ServletException {
    super.doFilter(arg0, arg1, arg2);
  }

  public void deactivate(Map<String, Object> properties) {
    extHttpService.unregisterFilter(this);
  }

  protected void bind(ExtHttpService extHttpService) {
    this.extHttpService = extHttpService;
  }

  protected void unbind(ExtHttpService extHttpService) {
    this.extHttpService = null;
  }

}
