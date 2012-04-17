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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
import org.apache.sanselan.ImageFormat;
import org.sakaiproject.nakamura.preview.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDFProcessor {

	private Logger log = LoggerFactory.getLogger(JODProcessor.class);

	/**
	 * Split a PDF by pages and store them in the previews directory as contentId.page.X
	 * @param inputPath
	 * @throws ProcessingException
	 */
	public void process(String inputPath, String outputPrefix) throws ProcessingException {
		File input = new File(inputPath);

		// Guess what this does? I'll give you $5
		PDFImageWriter imageWriter = new PDFImageWriter();
		try {
			// Load, split, loop and write an image of each page
			PDDocument document = PDDocument.load(input);
			int pageCount = document.getNumberOfPages();
			boolean success = imageWriter.writeImage(document,
					ImageFormat.IMAGE_FORMAT_JPEG.name, null,
					1, pageCount, outputPrefix);

			if (success) {
				log.debug("Wrote {} page image(s) to {}", pageCount, outputPrefix);
			}
			else {
				log.error("PDFBox was unable to save an image for document {} ", inputPath);
			}
		} catch (IOException e) {
			log.error("Error while splitting the PDF: {}", e);
			throw new ProcessingException("Error while splitting PDF at " + inputPath, e);
		}
	}
}
