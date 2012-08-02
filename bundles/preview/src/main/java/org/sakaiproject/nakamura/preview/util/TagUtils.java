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
package org.sakaiproject.nakamura.preview.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.sakaiproject.nakamura.api.termextract.ExtractedTerm;
import org.sakaiproject.nakamura.api.termextract.TermExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagUtils {

  private static Logger log = LoggerFactory.getLogger(TagUtils.class);

  /**
   * @param term extracted by the {@link TermExtractor}
   * @return whether or not to use a term as a tag
   */
  public static boolean isValidTag(ExtractedTerm term) {
    boolean valid = false;
    String t = term.getTerm();

    boolean isAlphaOrSpace = StringUtils.isAlphaSpace(t);
    boolean containsHttp = t.contains("http");
    boolean moreThanTwoWords = t.split(" ").length > 2;
    boolean number = false;
    try {
      Double.parseDouble(t);
      number = true;
    } catch (NumberFormatException e){
      // nothing to see here. Move along
    }
    if (t.length() > 1 && isAlphaOrSpace && !containsHttp && !moreThanTwoWords && !number){
      valid = true;
    }
    return valid;
  }

  /**
   * Tag a piece of content
   * @param id the id of the content item
   * @param tags the tags to use
   * @param remoteServer to connect to OAE
   */
  public static void tagContent(String id, List<String> tags, RemoteServerUtil remoteServer){
    log.info("Tagging {} with {}", id,  StringUtils.join(tags, ", "));
    
    NameValuePair[] parameters = new NameValuePair[tags.size() + 1];
    for (int i = 0; i < tags.size(); i++){
      parameters[i] = new NameValuePair("key", "/tags/" + tags.get(i));
    }
    parameters[tags.size()] = new NameValuePair(":operation", "tag");
    remoteServer.post("/p/" + id, parameters);
  }

  /**
   * Send an email to a user that their document was tagged
   * @param item
   * @param userId
   * @param tags
   * @param from
   * @param remoteServer
   */
  public static void doSendTagEmail(Map<String,Object> item, String userId, List<String> tags, String from, RemoteServerUtil remoteServer) {
    String originFileName = (String)item.get("sakai:pooled-content-file-name");
    VelocityEngine ve = new VelocityEngine();
    ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    ve.init();
    Template t = ve.getTemplate("tag-email.vm");
    VelocityContext context = new VelocityContext();
    context.put("origin_file_name", originFileName);
    context.put("tags", StringUtils.join(tags, "\n"));
    StringWriter writer = new StringWriter();
    t.merge(context, writer);

    ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new NameValuePair("sakai:type","internal"));
    params.add(new NameValuePair("sakai:sendstate", "pending"));
    params.add(new NameValuePair("sakai:messagebox", "outbox"));
    params.add(new NameValuePair("sakai:to", "internal:" + userId));
    params.add(new NameValuePair("sakai:from", from));
    params.add(new NameValuePair("sakai:subject", "We've added some tags to " + originFileName));
    params.add(new NameValuePair("sakai:body", writer.toString()));
    params.add(new NameValuePair("_charset_", "utf-8"));
    params.add(new NameValuePair("sakai:category", "message"));
    remoteServer.post("/~admin/message.create.json", (NameValuePair[])params.toArray());
  }
}
