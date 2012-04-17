package org.sakaiproject.nakamura.preview;

import static org.sakaiproject.nakamura.preview.util.HttpUtils.getHttpClient;
import static org.sakaiproject.nakamura.preview.util.HttpUtils.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class NakamuraFacade {

	private static final Logger log = LoggerFactory.getLogger(NakamuraFacade.class);

	protected URL server;
	protected String password;

	public NakamuraFacade(String server, String password) throws MalformedURLException {
		this(new URL(server), password);
	}

	public NakamuraFacade(URL server, String password){
		this.server = server;
		this.password = password;
	}

	public JSONObject get(String url){
		log.info("GET {}", url);
		GetMethod get = new GetMethod(url);
		JSONObject result = http(getHttpClient(server, "admin", password), get);
		return result;
	}

	public JSONObject post(String url, Map<String, String> params) {
		log.info("POST {}", url);
		PostMethod post = new PostMethod(url);
		for (Entry<String, String> entry: params.entrySet()){
			post.addParameter(entry.getKey(), entry.getValue());
		}
		return http(getHttpClient(server, "admin", password), post);
	}

	/**
	 * Mark content items we intend to process with our pid@host
	 * @param content the content to claim
	 * @param name the pid@host
	 */
	public void claimContent(List<Map<String,Object>> content, String name){
		JSONArray batch = new JSONArray();

		for (Map<String,Object> item: content) {
			JSONObject req = new JSONObject();
			JSONObject params = new JSONObject();
		    String path = (String)item.get("_path");
		    req.put("_charset_", "UTF-8");
		    req.put("url", "/p/" + path + ".json");
		    req.put("method", "POST");
		    params.put("sakai:processor", name);
		    req.put("parameters", params);
		    batch.add(req);
		}
		post("/system/batch", ImmutableMap.of("requests", batch.toString()));
	}

	/**
	 * Get the user.properties.isAutogagging values
	 * @param userId
	 * @return
	 */
	protected boolean doAutoTaggingForUser(String userId){
		boolean doAutoTagging = false;
		String userMetaUrl = "/system/me?uid=" + userId;
		log.debug("Fetching user metadata from {}", userMetaUrl);
		JSONObject userMeta = get(userMetaUrl);
		JSONObject props = userMeta.getJSONObject("user").getJSONObject("properties");
		if (props.has("isAutoTagging") && props.getBoolean("isAutoTagging")){
			doAutoTagging = true;
		}
		return doAutoTagging;
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
