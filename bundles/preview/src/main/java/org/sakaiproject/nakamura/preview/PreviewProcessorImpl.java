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
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
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
import org.sakaiproject.nakamura.preview.util.EasySSLProtocolSocketFactory;
import org.sakaiproject.nakamura.preview.util.FileListUtils;
import org.sakaiproject.nakamura.preview.util.HttpUtils;
import org.sakaiproject.nakamura.termextract.TermExtractorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class PreviewProcessorImpl {

	private static final Logger log = LoggerFactory.getLogger(PreviewProcessorImpl.class);

	protected URL server;
	protected URL contentServer;
	protected String password;
	protected String name;
	protected String basePath;

	protected Set<String> ignoreTypes;
	protected Set<String> mimeTypes;

	protected NakamuraFacade nakamura;
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
		this.nakamura = new NakamuraFacade(server, password);

		this.previewsDir = StringUtils.join(new String[] { basePath, "previews" }, File.separator );
		this.docsDir = StringUtils.join(new String[] { basePath, "docs" }, File.separator );
		this.termExtractor = new TermExtractorImpl(null);
	}

	public void process() throws IOException{
		createDirectories();

		List<Map<String,Object>> content = contentFetcher.getContentForProcessing(server.toString(), "admin", password);
		List<Map<String,Object>> ignored = new LinkedList<Map<String,Object>>();
		List<Map<String,Object>> failed  = new LinkedList<Map<String,Object>>();
		List<Map<String,Object>> processed  = new LinkedList<Map<String,Object>>();

		content = filterAlreadyProcessed(content);
		if (content.isEmpty()){
			return;
		}
		nakamura.claimContent(content, name);

		for (Map<String,Object> result : content){

			if (ignore(result)){
				ignored.add(result);
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
				Map<String,Object> item = nakamura.getContentMeta(id);

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
				processed.add(item);
			}
			catch (Exception e){
				log.error("There was an error generating a preview for {}: {}", id, e);
				failed.add(result);
			}
			finally {
				if (contentFilePath != null){
					File contentPathToDelete = new File(contentFilePath);
					contentPathToDelete.delete();
				}
				previewDir.delete();
			}
		}
		
		for(Map<String,Object> processedItem : processed){
			nakamura.post("/p/" + processedItem.get("_path") + ".json", ImmutableMap.of("sakai:needsprocessing", "false"));
			nakamura.post("/p/" + processedItem.get("_path") + ".json", ImmutableMap.of("sakai:hasPreview", "true"));
		}

		for(Map<String,Object> processedItem : failed){
			nakamura.post("/p/" + processedItem.get("_path") + ".json", ImmutableMap.of("sakai:needsprocessing", "false"));
			nakamura.post("/p/" + processedItem.get("_path") + ".json", ImmutableMap.of("sakai:processing_failed", "true"));
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
			if (nakamura.doAutoTaggingForUser(userId)){
				List<String> tags = new ArrayList<String>();
				for (ExtractedTerm term : termExtractor.process(textExtractor.getText(contentFilePath))){
					tags.add(term.getTerm());
				}
				nakamura.tagContent(id, tags);
				log.info("Tagged {} with {}", id, StringUtils.join(tags.toArray(), ","));
			}
		}

		String contentPreviewDirectory = StringUtils.join(new String[] { previewsDir, id }, File.separator);
		File[] previewFiles = FileListUtils.listFilesSortedName(contentPreviewDirectory);

		int i = 1;
		for (File preview: previewFiles){
			try {
				imageProcessor.resize(preview.getAbsolutePath(),
						contentPreviewDirectory + File.separator + id + ".large",
						new Double(700.0), null);
				nakamura.uploadFile(id, preview, Integer.toString(i), "large");

				imageProcessor.resize(preview.getAbsolutePath(),
						contentPreviewDirectory + File.separator + id + ".normal.jpg",
						new Double(700.0), null);
				nakamura.uploadFile(id, preview, Integer.toString(i), "normal");

				imageProcessor.resize(preview.getAbsolutePath(),
						contentPreviewDirectory + File.separator + id + ".small.jpg",
						new Double(180.0), new Double(225.0));
				nakamura.uploadFile(id, preview, Integer.toString(i), "small");
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
		String normalPath = previewsDir + File.separator + id + File.separator + id + ".normal.jpg";
		imageProcessor.resize(contentFilePath, normalPath, new Double(700.0), null);
		nakamura.uploadFile(id, new File(normalPath), "1", "normal");

		String smallPath = previewsDir + File.separator + id + File.separator + id + ".small.jpg";
		imageProcessor.resize(contentFilePath, smallPath, new Double(180.0), new Double(225.0));
		nakamura.uploadFile(id, new File(smallPath), "1", "small");
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
	 * Download a content body to a file
	 * @param address
	 * @param filePath
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public void download(String address, String filePath) throws Exception {
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
}
