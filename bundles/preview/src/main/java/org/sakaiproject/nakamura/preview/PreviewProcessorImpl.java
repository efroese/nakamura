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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.preview.ContentFetcher;
import org.sakaiproject.nakamura.api.preview.PreviewProcessor;
import org.sakaiproject.nakamura.api.termextract.ExtractedTerm;
import org.sakaiproject.nakamura.api.termextract.TermExtractor;
import org.sakaiproject.nakamura.preview.processors.ImageProcessor;
import org.sakaiproject.nakamura.preview.processors.JODProcessor;
import org.sakaiproject.nakamura.preview.processors.PDFProcessor;
import org.sakaiproject.nakamura.preview.processors.TikaTextExtractor;
import org.sakaiproject.nakamura.termextract.TermExtractorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class PreviewProcessorImpl {

	private static final Logger log = LoggerFactory.getLogger(PreviewProcessorImpl.class);

	protected URL server;
	protected String password;
	protected String name;
	protected String basePath;
	
	protected Set<String> ignoreTypes;
	protected Set<String> mimeTypes;

	protected ContentFetcher contentFetcher;

	protected ImageProcessor imageProcessor;
	protected JODProcessor jodProcessor;
	protected PDFProcessor pdfProcessor;
	protected TikaTextExtractor textExtractor;
	
	public PreviewProcessorImpl() {
		this.name = ManagementFactory.getRuntimeMXBean().getName();
		init();
	}

	public void init(){
		this.imageProcessor = new ImageProcessor();
		this.jodProcessor = new JODProcessor();
		this.pdfProcessor = new PDFProcessor();
		this.textExtractor = new TikaTextExtractor();
	}

	public void process() throws IOException{
		createDirectories();
		
		List<Map<String,Object>> content = contentFetcher.getContentForProcessing(server.toString(), "admin", password);
		List<Map<String,Object>> ignored = new LinkedList<Map<String,Object>>();
		List<Map<String,Object>> failed  = new LinkedList<Map<String,Object>>();

		content = filterAlreadyProcessed(content);
		if (content.isEmpty()){
			return;
		}
		claimContent(content);
		
		for (Map<String,Object> result : content){
			
			if (ignore(result)){
				ignored.add(result);
				continue;
			}
			String id = (String)result.get("_path");

			try {
				// Fetch the full content meta
				Map<String,Object> item = getContentMeta(id);
				
				// example: /var/sakaioae/pp/previews/s0meGr0SsId/
				String previewDirPath = StringUtils.join(
						new String[]{ basePath, "previews", id }, File.separator);
				File previewDir = new File(previewDirPath);
				previewDir.mkdirs();
				
				// Determine the correct file extension 
				String extension = null;
				if (item.containsKey(PreviewProcessor.FILE_EXTENSION)){
					extension = (String)item.get(PreviewProcessor.FILE_EXTENSION);
				}
				String mimetype = null;
				if (item.containsKey(PreviewProcessor.MIME_TYPE)){
					mimetype = (String)item.get(PreviewProcessor.MIME_TYPE);
				}
				extension = determineFileExtensionWithMimeType(mimetype, extension);

				String contentFilePath = StringUtils.join(
						new String[] { basePath, "docs", id + "." + extension }, File.separator);
				download(server + "/p/" + id, contentFilePath);
				
				if (PreviewProcessor.PDF_EXTENSIONS.contains(extension)){
					pdfProcessor.process(contentFilePath);
				}
				else if (!PreviewProcessor.IMAGE_EXTENSIONS.contains(extension)){
					// Not an image and not a PDF
					// Convert it to a pdf
					String convertedPDFPath = StringUtils.join(
							new String[] { basePath, "previews", id, id + ".pdf" }, File.separator);
					jodProcessor.process(contentFilePath, convertedPDFPath);
					// Split the PDF and snap images of the pages
					// They get saved to ${basePath}/previews/${id}/${id}.page.[1...n].JPEG
					pdfProcessor.process(convertedPDFPath);
					new File(convertedPDFPath).delete();
				}

				File[] previewFiles = previewDir.listFiles();
				Arrays.sort(previewFiles,
					new Comparator<File>(){
						public int compare(final File f1, final File f2) {
							return f1.getName().compareTo(f2.getName());
						}
					});

				if (previewFiles.length > 0){
					imageProcessor.resize(previewFiles[0].getAbsolutePath(),
							previewDir.getAbsolutePath() + File.separator + id + ".normal.jpg",
							new Double(700.0), null);
					imageProcessor.resize(previewFiles[0].getAbsolutePath(),
							previewDir.getAbsolutePath() + File.separator + id + ".small.jpg",
							new Double(180.0), new Double(225.0));
				}

				if (item.containsKey("sakai:pool-content-created-for")){
					String userId = (String)item.get("sakai:pool-content-created-for");
					if (generateTags(userId)){
						TermExtractor te = new TermExtractorImpl();
						List<ExtractedTerm> terms = te.process(textExtractor.getText(contentFilePath));
						List<String> tags = new ArrayList<String>();
						for (ExtractedTerm term : terms){
							tags.add(term.getTerm());
						}
						tagContent(id, tags);
					}
				}
			}
			catch (Exception e){
				log.error("There was an error generating a preview for {}: {}", id, e);
				failed.add(result);
			}
		}
	}

	/**
	 * Get the user.properties.isAutogagging values 
	 * @param userId
	 * @return
	 */
	protected boolean generateTags(String userId){
		boolean generateTags = false;
		String userMetaUrl = server + "/system/me?uid=" + userId;
		log.debug("Fetching user metadata from {}", userMetaUrl);
		GetMethod get = new GetMethod(userMetaUrl);
		JSONObject userMeta = HttpUtils.http(HttpUtils.getHttpClient(server, "admin", password), get);
		JSONObject props = userMeta.getJSONObject("user").getJSONObject("properties");
		if (props.has("isAutoTagging") && props.getBoolean("isAutoTagging")){
			generateTags = true;
		}
		return generateTags;
	}

	/**
	 * Tag a doc in OAE
	 * @param contentId
	 * @param tags
	 */
	protected void tagContent(String contentId, List<String> tags){
		PostMethod post = new PostMethod(server + "/p/" + contentId);
		post.addParameter(":operation", "tag");
		for (String tag: tags){
			post.addParameter("key", "/tags/" + tag);
		}
		HttpUtils.http(HttpUtils.getHttpClient(server, "admin", password), post);
	}

	/**
	 * Remove items from the list that the Preview Processor does not support
	 * @param content
	 * @return
	 */
	private boolean ignore(Map<String,Object> item) {
		boolean ignore = false;
		String mimetype = null;
		if (item.containsKey(PreviewProcessor.MIME_TYPE)){
			mimetype = StringUtils.trimToNull((String)item.get(PreviewProcessor.MIME_TYPE));
		}
		if (mimetype == null){
			ignore = true;
			log.info("Ignoring {}, no mimeType", (String)item.get("_path"));
		}
		else if (ignoreTypes.contains(mimetype)){
			ignore = true;
			log.info("Ignoring {}, mimeType {} is not supported.", (String)item.get("_path"), mimetype);
		}
		return ignore;
	}

	protected String determineFileExtensionWithMimeType(String mimetype, String extension){
		if (mimetype == null && extension == null){
			return null;
		}
		if (extension.startsWith(".")){
			extension = extension.substring(1, extension.length());
		}
		for (String mime: mimeTypes){
			if (mime.contains(mimetype) && !mime.contains(extension)){
				String[] split = mime.split("");
				if (split.length > 1){
					extension = split[1];
				}
			}
		}
		return extension;
	}

	/**
	 * Create the folders we need.
	 */
	private void createDirectories(){
		File baseDir = new File(basePath);
		baseDir.mkdirs();
		for (String sub : new String[] { "docs", "previews", "logs" }){
			new File(this.basePath + File.separator + sub).mkdirs();
		}
	}

	/**
	 * Find content items we need to process.
	 * @param content
	 * @return
	 */
	protected List<Map<String,Object>> filterAlreadyProcessed(List<Map<String,Object>> content){
		List<Map<String,Object>> unprocessed = new LinkedList<Map<String,Object>>();
		for (Map<String,Object> item : content){
			// No one else has claimed this 
			if ( !item.containsKey(PreviewProcessor.PROCESSED_BY) || 
					(item.containsKey(PreviewProcessor.PROCESSED_BY) &&
						!name.equals(item.get(PreviewProcessor.PROCESSED_BY)))){
				unprocessed.add(item);
			}
		}
		return unprocessed;
	}

	/**
	 * Mark content items we intend to process with our pid@host
	 * @param content
	 */
	public void claimContent(List<Map<String,Object>> content){
		JSONArray batch = new JSONArray();

		for (Map<String,Object> item: content) {
			JSONObject req = new JSONObject();
			JSONObject params = new JSONObject();
		    String path = (String)item.get("_path");
		    req.put("_charset_", "UTF-8");
		    req.put("url", "/p/" + path + ".json");
		    params.put("sakai:processor", this.name);
		    req.put("params", params);
		    batch.add(req);
		}
		PostMethod post = new PostMethod("/system/batch");
		post.addParameter("requests", batch.toString());
		HttpUtils.http(HttpUtils.getHttpClient(server, "admin", password), post);
	}

	/**
	 * Download a content body to a file
	 * @param address
	 * @param filePath
	 */
	public void download(String address, String filePath) {
		OutputStream output = null;
		InputStream input = null;
		try {
			input = new URL(address).openConnection().getInputStream();
			output = FileUtils.openOutputStream(new File(filePath));
			IOUtils.copy(input, output);
			log.info("Downloaded content body {} to {}", address, filePath);
		}
		catch (Exception e) {
			log.error("Error downloading content: {}", e);
		}
		finally {
			try {
				if (input != null) { input.close(); }
			} catch (IOException e) {
				log.error("Error closing input stream: {}", e);
			}
			try {
				if (output != null) { output.close(); }
			} catch (IOException e) {
				log.error("Error closing output stream: {}", e);
			}
		}
	}

	/**
	 * Get the content metadata
	 * @param contentId
	 * @return
	 */
	protected Map<String,Object> getContentMeta(String contentId){
		String url = server + "/p/" + contentId + ".json";
		log.info("Downloading content metadata from {}", url);
		GetMethod get = new GetMethod(url);
		JSONObject result = HttpUtils.http(HttpUtils.getHttpClient(server, "admin", password), get);
		return ImmutableMap.copyOf(result);
	}
}
