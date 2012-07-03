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
package org.sakaiproject.nakamura.preview;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.preview.ContentFetcher;
import org.sakaiproject.nakamura.preview.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service
public class SearchContentFetcher implements ContentFetcher {

  private static final Logger log = LoggerFactory.getLogger(SearchContentFetcher.class);

  @SuppressWarnings("unchecked")
  @Override
  public List<Map<String, Object>> getContentForProcessing(String serverUrl, String username, String password) {
    List<Map<String,Object>> content = new LinkedList<Map<String,Object>>();
    String needsprocessingUrl = "/var/search/needsprocessing.json";
    HttpMethod get = new GetMethod(needsprocessingUrl);
    try {
      log.info("Fetching new content from {}", needsprocessingUrl);
      JSONObject response = HttpUtils.http(HttpUtils.getHttpClient(new URL(serverUrl), username, password), get);
      JSONArray results = response.getJSONArray("results");
      log.debug("Fetched {} new content item(s) for processing: {}", results.size());
      log.trace(results.toString());
      for (int i = 0; i < results.size(); i++){
        content.add(results.getJSONObject(i));
      }
    } catch (JSONException e){
      log.error("Error parsing needsprocessing JSON response: ", e);
    } catch (IOException e) {
      log.error("Network error while fetching the needsprocessing search results: ", e);
    }
    return content;
  }
}
