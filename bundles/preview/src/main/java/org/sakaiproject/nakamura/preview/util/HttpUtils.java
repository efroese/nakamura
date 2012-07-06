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

import java.net.URL;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

	public static Logger log = LoggerFactory.getLogger(HttpUtils.class);

	private static final String HTTP_USER_AGENT = HttpUtils.class.getName();

	/**
	 * Construct an {@link HttpClient} which is configured to authenticate to OAE.
	 * @return the configured client.
	 */
	public static HttpClient getHttpClient(URL url, String username, String password){
		HttpClient client = new HttpClient();
		HttpState state = client.getState();
		state.setCredentials(
			new AuthScope(url.getHost(), getPort(url)),
			new UsernamePasswordCredentials(username, password));
		client.getParams().setAuthenticationPreemptive(true);
		client.getParams().setParameter("http.useragent", HTTP_USER_AGENT);
		client.getParams().setParameter("_charset_", "utf-8");

		HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHost(url.getHost(), (url.getPort() == -1)? url.getDefaultPort(): url.getPort(), url.getProtocol());

		if ("https".equals(url.getProtocol().toLowerCase())){
  		ProtocolSocketFactory factory = new EasySSLProtocolSocketFactory();
  		Protocol easyHttps = new Protocol(url.getProtocol(), factory, getPort(url));
  		hostConfiguration.setHost(url.getHost(), getPort(url), easyHttps);
		}
		client.setHostConfiguration(hostConfiguration);
		return client;
	}

	/**
	 * If you don't specify a port when creating a {@link URL} {@link URL#getPort()} will return -1.
	 * This function uses the default HTTP/s ports
	 * @return the port for this.url. 80 or 433 if not specified.
	 */
	public static int getPort(URL url){
		int port = url.getPort();
		if (port == -1){
			if (url.getProtocol().equals("http")){
				port = 80;
			}
			else if(url.getProtocol().equals("https")){
				port = 443;
			}
		}
		return port;
	}

	public static JSONObject http(HttpClient client, HttpMethod method, int timeout) {
	  HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
	  params.setSoTimeout(timeout);
	  client.getHttpConnectionManager().setParams(params);
	  return HttpUtils.http(client, method);
	}

	/**
	 * Prepare an HTTP request to Sakai OAE and parse the response (if JSON).
	 * @param client an {@link HttpClient} to execute the request.
	 * @param method an HTTP method to send
	 * @return a JSONObject of the response if the request returns JSON
	 */
	public static JSONObject http(HttpClient client, HttpMethod method) {

		method.setRequestHeader("User-Agent", HTTP_USER_AGENT);
		if (client.getHostConfiguration() != null && client.getHostConfiguration().getHost() != null){
			method.setRequestHeader("Referer", client.getHostConfiguration().getHostURL());
		}

		String errorMessage = null;
		String responseString = null;
		JSONObject responseJSON = null;

		boolean isJSONRequest = ! method.getPath().toString().endsWith(".html");

		if (log.isDebugEnabled() && method instanceof PostMethod){
			log.debug(method.getName() + " " + method.getPath() + " params:");
			for (NameValuePair nvp : ((PostMethod)method).getParameters()){
				log.debug(nvp.getName() + " = " + nvp.getValue());
			}
		}

		int responseCode = -1;
		try{
			responseCode = client.executeMethod(client.getHostConfiguration(), method);
			responseString = StringUtils.trimToNull(IOUtils.toString(method.getResponseBodyAsStream()));

			if(isJSONRequest){
				responseJSON = parseJSONResponse(responseString);
			}

			if(log.isDebugEnabled()){
				log.debug(responseCode + " " + method.getName() + " " + method.getPath());
			}
			if (log.isTraceEnabled()){
				log.trace("reponse: " + responseString);
			}

			switch (responseCode){

			case HttpStatus.SC_OK: // 200
			case HttpStatus.SC_CREATED: // 201
				break;
			case HttpStatus.SC_BAD_REQUEST: // 400
			case HttpStatus.SC_UNAUTHORIZED: // 401
			case HttpStatus.SC_NOT_FOUND: // 404
			case HttpStatus.SC_INTERNAL_SERVER_ERROR: // 500
				if (isJSONRequest && responseJSON != null){
					errorMessage = responseJSON.getString("status.message");
				}
				if (errorMessage == null){
					errorMessage = "Empty "+ responseCode + " error. Check the logs on the Sakai OAE server.";
				}
				break;
			default:
				errorMessage = "Unknown HTTP response " + responseCode;
				break;
			}
		}
		catch (Exception e) {
			errorMessage = "An exception occurred communicatingSakai OAE. " + e.toString();
		}
		finally {
			method.releaseConnection();
		}
		return responseJSON;
	}

	/**
	 * Try to parse the HTTP response as JSON.
	 * @param response the HTTP response body as a String.
	 * @return a JSONObject representing the parsed response. null if not JSON or null.
	 */
	protected static JSONObject parseJSONResponse(String response){
		JSONObject json = null;
		if (response != null){
			try {
				json = JSONObject.fromObject(response);
			}
			catch (JSONException je){
				if (response.startsWith("<html>")){
					log.debug("Expected a JSON response, got html");
				}
				else {
					log.error("Could not parse JSON response. " + response);
				}
			}
		}
		return json;
	}
}
