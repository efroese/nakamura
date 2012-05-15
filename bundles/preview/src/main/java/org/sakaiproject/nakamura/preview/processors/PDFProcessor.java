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
package org.sakaiproject.nakamura.preview.processors;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
import org.apache.sanselan.ImageFormat;
import org.sakaiproject.nakamura.preview.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Render a series of images from PDF pages.
 */
public class PDFProcessor implements Callable<Integer> {

	private static final Logger log = LoggerFactory.getLogger(JODProcessor.class);

	private String inputPath;
	private String outputPrefix;
	private int numPages;

	/**
	 * @param inputPath the path to a PDF file
	 * @param outputPrefix a prefix to append pageN.jpeg to
	 * @param numPages how many pages to process, -1 for all pages
	 */
	public PDFProcessor(String inputPath, String outputPrefix, int numPages){
	  this.inputPath = inputPath;
	  this.outputPrefix = outputPrefix;
	  this.numPages = numPages;
	}

  @Override
  public Integer call() throws Exception {
    return process();
  }
	
	/**
	 * Split a PDF by pages and store them in the previews directory as contentId.pageX.jpeg
	 * @throws IOException 
	 */
	public Integer process() throws ProcessingException, IOException {
		PDFImageWriter imageWriter = new PDFImageWriter();
		// Load, split, loop and write an image of each page
		PDDocument document = PDDocument.load(new File(inputPath));
		int pageCount = (numPages < 0)? document.getNumberOfPages() : 1;
		boolean success = imageWriter.writeImage(document,
		    ImageFormat.IMAGE_FORMAT_JPEG.name,
		    null, // password
		    1,    // start
		    pageCount,
		    outputPrefix);

		if (success) {
			log.debug("Wrote {} page image(s) to {}", pageCount, outputPrefix);
			return pageCount;
		}
		else {
			log.error("PDFBox was unable to save an image for document {} ", inputPath);
			return 0;
		}
	}
}
