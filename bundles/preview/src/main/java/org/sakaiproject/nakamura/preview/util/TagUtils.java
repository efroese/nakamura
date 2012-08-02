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

import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
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
    Collections.sort(tags);
    log.info("Tagging {} with {}", id,  StringUtils.join(tags, ", "));
    
    NameValuePair[] parameters = new NameValuePair[tags.size() + 1];
    for (int i = 0; i < tags.size(); i++){
      parameters[i] = new NameValuePair("key", "/tags/" + tags.get(i));
    }
    parameters[tags.size()] = new NameValuePair(":operation", "tag");
    remoteServer.post("/p/" + id, parameters);
  }
}
