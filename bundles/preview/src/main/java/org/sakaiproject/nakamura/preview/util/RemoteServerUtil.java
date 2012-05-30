package org.sakaiproject.nakamura.preview.util;

import static org.sakaiproject.nakamura.preview.util.HttpUtils.getHttpClient;
import static org.sakaiproject.nakamura.preview.util.HttpUtils.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteServerUtil {

  private static final Logger log = LoggerFactory.getLogger(RemoteServerUtil.class);

  private static final int NO_TIMEOUT = 0;

	protected URL server;
	protected String password;

	public RemoteServerUtil(String server, String password) throws MalformedURLException {
		this(new URL(server), password);
	}

	public RemoteServerUtil(URL server, String password){
		this.server = server;
		this.password = password;
	}

	public JSONObject get(String url){
		log.debug("GET {}", url);
		GetMethod get = new GetMethod(url);
		JSONObject result = http(getHttpClient(server, "admin", password), get);
		return result;
	}

	public JSONObject post(String url, Map<String, String> params) {
		return post(url, params, NO_TIMEOUT);
	}

	public JSONObject post(String url, Map<String, String> params, int timeout) {
    log.debug("POST {}", url);
    PostMethod post = new PostMethod(url);
    for (Entry<String, String> entry: params.entrySet()){
      post.addParameter(entry.getKey(), entry.getValue());
    }
    return http(getHttpClient(server, "admin", password), post, timeout);
  }

	/**
	 * Upload a content preview image
	 * @param contentId the id of the content item (_path)
	 * @param preview the file to upload
	 * @param page the page number
	 * @param size the size identifier (small, normal, large)
	 * @throws FileNotFoundException
	 */
	public void uploadContentPreview(String contentId, File preview, String page, String size) throws FileNotFoundException {
		PostMethod post = new PostMethod("/system/pool/createfile." + contentId + ".page" + page + "-" + size);
		Part part = new FilePart("thumbnail", preview);
		MultipartRequestEntity entity = new MultipartRequestEntity(new Part[]{ part }, post.getParams());
		post.setRequestEntity(entity);
		http(getHttpClient(server, "admin", password), post);

		String altUrl = "/p/" + contentId + "/page" + page + "." + size + ".jpg";
		post = new PostMethod(altUrl);
		post.addParameter("sakai:excludeSearch", "true");
		http(getHttpClient(server, "admin", password), post);
		log.info("Uploaded {}{}", server.toString(), altUrl);
	}
}
