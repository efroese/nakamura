package org.sakaiproject.nakamura.preview;

import static org.sakaiproject.nakamura.preview.util.HttpUtils.getHttpClient;
import static org.sakaiproject.nakamura.preview.util.HttpUtils.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

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
		    params.put("sakai:processor", name);
		    req.put("params", params);
		    batch.add(req);
		}
		PostMethod post = new PostMethod("/system/batch");
		post.addParameter("requests", batch.toString());
		http(getHttpClient(server, "admin", password), post);
	}

	/**
	 * Get the user.properties.isAutogagging values
	 * @param userId
	 * @return
	 */
	protected boolean doAutoTaggingForUser(String userId){
		boolean generateTags = false;
		String userMetaUrl = server + "/system/me?uid=" + userId;
		log.debug("Fetching user metadata from {}", userMetaUrl);
		GetMethod get = new GetMethod(userMetaUrl);
		JSONObject userMeta = http(getHttpClient(server, "admin", password), get);
		JSONObject props = userMeta.getJSONObject("user").getJSONObject("properties");
		if (props.has("isAutoTagging") && props.getBoolean("isAutoTagging")){
			generateTags = true;
		}
		return generateTags;
	}

	/**
	 * Get the content metadata
	 * @param contentId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object> getContentMeta(String contentId){
		String url = server + "/p/" + contentId + ".json";
		log.info("Downloading content metadata from {}", url);
		GetMethod get = new GetMethod(url);
		JSONObject result = http(getHttpClient(server, "admin", password), get);
		return ImmutableMap.copyOf(result);
	}

	/**
	 * Tag a doc in OAE
	 * @param contentId
	 * @param tags
	 */
	public void tagContent(String contentId, List<String> tags){
		PostMethod post = new PostMethod(server + "/p/" + contentId);
		post.addParameter(":operation", "tag");
		for (String tag: tags){
			post.addParameter("key", "/tags/" + tag);
		}
		http(getHttpClient(server, "admin", password), post);
	}

	public void uploadPreviewFile(String id, int page, String size, File preview) throws FileNotFoundException {
		PostMethod post = new PostMethod("/system/pool/createfile/" + id + ".page" + page + "-" + size);
		Part part = new FilePart("thumbnail", preview);
		MultipartRequestEntity entity = new MultipartRequestEntity(new Part[]{ part }, post.getParams());
		post.setRequestEntity(entity);
		http(getHttpClient(server, "admin", password), post);

		post = new PostMethod("/p/" + id + "/page" + page + "." + size + ".jpg");
		post.addParameter("sakai:excludeSearch", "true");
		http(getHttpClient(server, "admin", password), post);
	}

}
