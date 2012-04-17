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
import org.apache.commons.lang.StringUtils;
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
		    req.put("method", "POST");
		    params.put("sakai:processor", name);
		    req.put("parameters", params);
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
		String userMetaUrl = "/system/me?uid=" + userId;
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
		String url = "/p/" + contentId + ".json";
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
		if (tags != null && !tags.isEmpty()){
			PostMethod post = new PostMethod("/p/" + contentId);
			post.addParameter(":operation", "tag");
			// key => /tags/merp/tags/uh derp/tags/blerp
			String tagString = "/tags/" + StringUtils.join(tags, "/tags/");
			post.addParameter("key", tagString);
			log.info("Tagging {} with {}", contentId, tagString);
			http(getHttpClient(server, "admin", password), post);
		} else {
			log.info("No tags provided for {}", contentId);
		}
	}

	public void uploadFile(String contentId, File content, String page, String size) throws FileNotFoundException {
		PostMethod post = new PostMethod("/system/pool/createfile." + contentId + ".page" + page + "-" + size);
		Part part = new FilePart("thumbnail", content);
		MultipartRequestEntity entity = new MultipartRequestEntity(new Part[]{ part }, post.getParams());
		post.setRequestEntity(entity);
		http(getHttpClient(server, "admin", password), post);

		String altUrl = "/p/" + contentId + "/page" + page + "." + size + ".jpg";
		post = new PostMethod(altUrl);
		post.addParameter("sakai:excludeSearch", "true");
		http(getHttpClient(server, "admin", password), post);
		log.info("Uploaded {}{}", server.toString(), altUrl);
	}

	public JSONObject post(String url, Map<String, String> params) {
		PostMethod post = new PostMethod(url);
		for (Entry<String, String> entry: params.entrySet()){
			post.addParameter(entry.getKey(), entry.getValue());
		}
		return http(getHttpClient(server, "admin", password), post);
	}

}
