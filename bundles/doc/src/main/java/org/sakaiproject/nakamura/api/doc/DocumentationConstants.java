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
package org.sakaiproject.nakamura.api.doc;


public interface DocumentationConstants {

  public static final CharSequence HTML_HEADER = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 TRANSITIONAL//EN\">"
  + "<html><head>"
  + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
  + "<link rel=\"stylesheet\" type=\"text/css\" href=\"/sling.css\" >"
  + "<link rel=\"stylesheet\" type=\"text/css\" href=\""
  + DocumentationConstants.PREFIX
  + "?p=style\">"
  + "</head><body>";
  public static final CharSequence HTML_FOOTER = "</body></html>";
  public static final String PREFIX = "/system/doc";

  public static final CharSequence CSS_CLASS_DOCUMENTATION_LIST = "documentation-list";
  public static final CharSequence CSS_CLASS_NODOC = "nodoc";
  public static final CharSequence CSS_CLASS_PARAMETERS = "parameters";
  public static final CharSequence CSS_CLASS_PARAMETER_NAME = "parameter-name";
  public static final CharSequence CSS_CLASS_PARAMETER_DESCRIPTION = "parameter-description";
  public static final CharSequence CSS_CLASS_PATH = "path";
  public static final CharSequence CSS_CLASS_SHORT_DESCRIPTION = "short-description";
  public static final CharSequence CSS_CLASS_VERSIONS = "versions";
  public static final CharSequence CSS_CLASS_VERSION_WARNING = "version-warning";

}
