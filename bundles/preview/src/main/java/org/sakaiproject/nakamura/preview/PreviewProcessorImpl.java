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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.sakaiproject.nakamura.api.preview.ContentFetcher;
import org.sakaiproject.nakamura.api.preview.PreviewProcessor;
import org.sakaiproject.nakamura.api.termextract.ExtractedTerm;
import org.sakaiproject.nakamura.api.termextract.TermExtractor;
import org.sakaiproject.nakamura.preview.processors.ImageProcessor;
import org.sakaiproject.nakamura.preview.processors.JODProcessor;
import org.sakaiproject.nakamura.preview.processors.PDFProcessor;
import org.sakaiproject.nakamura.preview.processors.TikaTextExtractor;
import org.sakaiproject.nakamura.preview.util.EasySSLProtocolSocketFactory;
import org.sakaiproject.nakamura.preview.util.FileListUtils;
import org.sakaiproject.nakamura.preview.util.HttpUtils;
import org.sakaiproject.nakamura.preview.util.RemoteServerUtil;
import org.sakaiproject.nakamura.termextract.DefaultFilter;
import org.sakaiproject.nakamura.termextract.TermExtractorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class PreviewProcessorImpl {

  private static final Logger log = LoggerFactory.getLogger(PreviewProcessorImpl.class);

  // Tags
  // If a tag is one word is must occur n time in the doc text
  private static final int SINGLE_WORD_TERM_MIN_OCCUR = 4;
  private static final int TERM_MIN_WORDS = 1;
  private static final int TERM_MAX_WORDS = 2;
  private static final int TERM_MIN_LENGTH = 2;
  private static final int TERM_MAX_LENGTH = 128;

  // Images
  private static final Double SMALL_MAX_WIDTH = new Double(180.0);
	private static final Double SMALL_MAX_HEIGHT = new Double(225.0);
	private static final Double LARGE_MAX_WIDTH = new Double(700.0);

	private static final int MAX_TAGS = 10;

	protected URL server;
	protected URL contentServer;
	protected String password;
	protected String name;
	protected String basePath;

	protected Set<String> ignoreTypes;
	protected Set<String> mimeTypes;

	protected RemoteServerUtil nakamura;
	protected ContentFetcher contentFetcher;

	protected ImageProcessor imageProcessor;
	protected JODProcessor jodProcessor;
	protected PDFProcessor pdfProcessor;
	protected TikaTextExtractor textExtractor;
	protected TermExtractor termExtractor;

	protected String previewsDir;
	protected String docsDir;


	public PreviewProcessorImpl() {
		this.name = ManagementFactory.getRuntimeMXBean().getName();
		init();
	}

	public void init(){
		this.imageProcessor = new ImageProcessor();
		this.jodProcessor = new JODProcessor();
		this.pdfProcessor = new PDFProcessor();
		this.textExtractor = new TikaTextExtractor();
		this.nakamura = new RemoteServerUtil(server, password);

		this.previewsDir = StringUtils.join(new String[] { basePath, "previews" }, File.separator );
		this.docsDir = StringUtils.join(new String[] { basePath, "docs" }, File.separator );

		this.termExtractor = new TermExtractorImpl(null,
		    new DefaultFilter(SINGLE_WORD_TERM_MIN_OCCUR, TERM_MIN_WORDS, 
		        TERM_MAX_WORDS, TERM_MIN_LENGTH, TERM_MAX_LENGTH));
	}

	public void process() throws IOException {

	  createDirectories();

		List<Map<String,Object>> content =
		  contentFetcher.getContentForProcessing(server.toString(), "admin", password);

		content = filterAlreadyProcessed(content);
		if (content.isEmpty()){
			return;
		}

		claimContent(content, name);

		for (Map<String,Object> result : content){
			if (ignore(result)){
				continue;
			}
			String id = (String)result.get("_path");
			String contentFilePath = null;
			// example: /var/sakaioae/pp/previews/s0meGr0SsId/
			String previewDirPath = StringUtils.join(new String[]{ previewsDir, id }, File.separator);
			File previewDir = new File(previewDirPath);

			try {
				previewDir.mkdirs();

				// Fetch the full content meta
				@SuppressWarnings("unchecked")
				Map<String,Object> item = ImmutableMap.copyOf(nakamura.get("/p/" + id + ".json"));

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

				contentFilePath = StringUtils.join(
						new String[] { docsDir, id + "." + extension }, File.separator);
				download(server + "/p/" + id, contentFilePath);

				if (PreviewProcessor.IMAGE_EXTENSIONS.contains(extension)){
					processImage(contentFilePath, item);
				}
				else {
					int pageCount = processDocument(contentFilePath, item, extension);
					nakamura.post("/p/" + id + ".json", ImmutableMap.of("sakai:pagecount", Integer.toString(pageCount)));
				}

				nakamura.post("/p/" + id + ".json", ImmutableMap.of(PreviewProcessor.NEEDS_PROCESSING, "false"));
	      log.info("Set {}  {}={}", new String[] { id, PreviewProcessor.NEEDS_PROCESSING, "false"});
	      nakamura.post("/p/" + id + ".json", ImmutableMap.of(PreviewProcessor.HAS_PREVIEW, "true"));
	      log.info("Set {}  {}={}", new String[] { id, PreviewProcessor.HAS_PREVIEW, "true"});
			}
			catch (Exception e){
				log.error("There was an error generating a preview for {}", id, e);
				nakamura.post("/p/" + id + ".json", ImmutableMap.of(PreviewProcessor.NEEDS_PROCESSING, "false"));
	      log.info("Set {}  {}={}", new String[] { id, PreviewProcessor.NEEDS_PROCESSING, "false"});
	      nakamura.post("/p/" + id + ".json", ImmutableMap.of(PreviewProcessor.PROCESSING_FAILED, "true"));
	      log.info("Set {}  {}={}", new String[] { id, PreviewProcessor.PROCESSING_FAILED, "true"});
			}
			finally {
				if (contentFilePath != null){
					File contentPathToDelete = new File(contentFilePath);
					contentPathToDelete.delete();
				}
				FileUtils.deleteDirectory(previewDir);
			}
		}
	}

	/**
	 * Download a content body to a file
	 * @param address
	 * @param filePath
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	protected void download(String address, String filePath) throws Exception {
		OutputStream output = null;
		try {
			URL url = new URL(address);
			GetMethod get = new GetMethod(url.getPath());
			HttpClient client = HttpUtils.getHttpClient(contentServer, "admin", password);
			HostConfiguration hc = new HostConfiguration();
			hc.setHost(new HttpHost(contentServer.getHost(), contentServer.getPort(),
					new Protocol(contentServer.getProtocol(), new EasySSLProtocolSocketFactory(), contentServer.getDefaultPort())));
			int responseCode = client.executeMethod(hc, get);
			if (responseCode == HttpStatus.SC_OK){
				output = FileUtils.openOutputStream(new File(filePath));
				IOUtils.copy(get.getResponseBodyAsStream(), output);
				log.info("Downloaded content body {} to {}", address, filePath);
			}
			else {
				throw new Exception("Error downloading content. Response code was " + responseCode);
			}
		}
		catch (Exception e) {
			log.error("Error downloading content: {}", e);
			throw e;
		}
		finally {
			try {
				if (output != null) { output.close(); }
			} catch (IOException e) {
				log.error("Error closing output stream: {}", e);
			}
		}
	}

	/**
	 * Convert, split, and render previews for a document.
	 * @param contentFilePath
	 * @param item
	 * @param extension
	 * @return
	 * @throws ProcessingException
	 * @throws FileNotFoundException
	 */
	protected int processDocument(String contentFilePath, Map<String,Object> item, String extension)
	throws ProcessingException, FileNotFoundException{
		String id = (String)item.get("_path");

		String outputPrefix = StringUtils.join(
				new String[]{ previewsDir, id, "page." }, File.separator);
		if (PreviewProcessor.PDF_EXTENSIONS.contains(extension)){
			// Split the PDF and snap images of the pages
			// Images are saved to ${basePath}/previews/${id}/${id}.page.[1...n].JPEG
			pdfProcessor.process(contentFilePath, outputPrefix);
		}
		else {
			// Not an image and not a PDF
			// Convert it to a pdf
			String convertedPDFPath = StringUtils.join(
					new String[] { previewsDir, id, id + ".pdf" }, File.separator);
			jodProcessor.process(contentFilePath, convertedPDFPath);
			// Split the PDF and snap images of the pages
			// Images are saved to ${basePath}/previews/${id}/${id}.page.[1...n].JPEG
			pdfProcessor.process(convertedPDFPath, outputPrefix);
		}

		if (item.containsKey("sakai:pool-content-created-for")){
			String userId = (String)item.get("sakai:pool-content-created-for");
			JSONObject userMeta = getUserMeta(userId);
			List<String> tags = new ArrayList<String>();

			if (doAutoTaggingForUser(userMeta)){
				List<ExtractedTerm> terms = termExtractor.process(textExtractor.getText(contentFilePath));
				// sort by occurrences * strength
				Collections.sort(terms, new Comparator<ExtractedTerm>() {
          @Override
          public int compare(ExtractedTerm o1, ExtractedTerm o2) {
            return Integer.valueOf(o1.getOccurences() * o1.getStrength()).compareTo(o2.getOccurences() * o2.getStrength());
          }
        });

				int i = 0;
        for (ExtractedTerm term : terms){
          String t = term.getTerm();
            if (i < MAX_TAGS && t.length() > 1){
              try {
                Double.parseDouble(t);
                continue;
              }
              catch (NumberFormatException e) {
                // ignore
              }
              tags.add(term.getTerm());
              i++;
            }
        }

				if (tags != null && !tags.isEmpty()){
					String tagString = "/tags/" + StringUtils.join(tags, "/tags/");
					log.info("Tagging {} with {}", id, tagString);
					nakamura.post("/p/" + id, ImmutableMap.of(":operation", "tag", "key", tagString));
					if (sendTagEmail(userMeta)){
						doSendTagEmail(item, userId, tags);
					}
				} else {
					log.info("No tags generated for {}", id);
				}

			}
		}

		String contentPreviewDirectory = StringUtils.join(new String[] { previewsDir, id }, File.separator);
		File[] previewFiles = FileListUtils.listFilesSortedName(contentPreviewDirectory);

		int i = 1;
		for (File preview: previewFiles){
		  if (!preview.getPath().toLowerCase().endsWith(".jpg")){
		    continue;
		  }
			try {
				imageProcessor.resize(preview.getAbsolutePath(),
						contentPreviewDirectory + File.separator + id + ".large.jpg",
						LARGE_MAX_WIDTH, null);
				nakamura.uploadContentPreview(id, preview, Integer.toString(i), "large");

				imageProcessor.resize(preview.getAbsolutePath(),
						contentPreviewDirectory + File.separator + id + ".normal.jpg",
						LARGE_MAX_WIDTH, null);
				nakamura.uploadContentPreview(id, preview, Integer.toString(i), "normal");

				imageProcessor.resize(preview.getAbsolutePath(),
						contentPreviewDirectory + File.separator + id + ".small.jpg",
						SMALL_MAX_WIDTH, SMALL_MAX_HEIGHT);
				nakamura.uploadContentPreview(id, preview, Integer.toString(i), "small");
			}
			catch (ProcessingException e) {
				log.error("Error uploading content preview.", e);
			}
			i++;
		}
		return previewFiles.length;
	}

	protected void processImage(String contentFilePath, Map<String,Object> item)
	throws ProcessingException, FileNotFoundException {
		String id = (String)item.get("_path");
		String prefix = previewsDir + File.separator + id + File.separator + id;

		String normalPath = prefix + ".normal.jpg";
		imageProcessor.resize(contentFilePath, normalPath, LARGE_MAX_WIDTH, null);
		nakamura.uploadContentPreview(id, new File(normalPath), "1", "normal");

		String smallPath = prefix + ".small.jpg";
		imageProcessor.resize(contentFilePath, smallPath, SMALL_MAX_WIDTH, SMALL_MAX_HEIGHT);
		nakamura.uploadContentPreview(id, new File(smallPath), "1", "small");
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
			log.info("Ignoring {}, no mimeType", item.get("_path"));
		}
		else if (ignoreTypes.contains(mimetype)){
			ignore = true;
			log.info("Ignoring {}, mimeType {} is not supported.", item.get("_path"), mimetype);
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
	 * @param content the content to claim
	 * @param name the pid@host
	 */
	protected void claimContent(List<Map<String,Object>> content, String name){
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
		nakamura.post("/system/batch", ImmutableMap.of("requests", batch.toString()));
	}

	protected JSONObject getUserMeta(String userId){
		log.debug("Fetching user metadata for {}", userId);
		return nakamura.get("/system/me?uid=" + userId);
	}

	// -------------- Tagging ---------------

	protected boolean doAutoTaggingForUser(JSONObject userMeta){
		JSONObject props = userMeta.getJSONObject("user").getJSONObject("properties");
		return props.has("isAutoTagging") && props.getBoolean("isAutoTagging");
	}

	protected boolean sendTagEmail(JSONObject userMeta){
		JSONObject props = userMeta.getJSONObject("user").getJSONObject("properties");
		return props.has("sendTagMsg") && props.getBoolean("sendTagMsg");
	}

	private void doSendTagEmail(Map<String,Object> item, String userId, List<String> tags) {
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

		Builder<String,String> paramBuilder = ImmutableMap.builder();
		paramBuilder.put("sakai:type","internal");
		paramBuilder.put("sakai:sendstate", "pending");
		paramBuilder.put("sakai:messagebox", "outbox");
		paramBuilder.put("sakai:to", "internal:" + userId);
		paramBuilder.put("sakai:from", "admin");
		paramBuilder.put("sakai:subject", "We've added some tags to " + originFileName);
		paramBuilder.put("sakai:body", writer.toString());
		paramBuilder.put("_charset_", "utf-8");
		paramBuilder.put("sakai:category", "message");
		nakamura.post("/~admin/message.create.json", paramBuilder.build());
	}
}
