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
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.preview.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

public class JODProcessor {

	private Logger log = LoggerFactory.getLogger(JODProcessor.class);
	
	public List<String> process(String inputPath) throws ProcessingException {

		List<String> previewPaths = new ArrayList<String>();
		// example: 
		// inputPath  = /var/sakaioae/pp/docs/s0m3grOssId
		// ppBase     = /var/sakaioae/pp
		// contentId  = s0m3grOssId
		// outputPath = /var/sakaioae/pp/previews/s0m3grOssId
		File input = new File(inputPath);
		String contentId = input.getName();
		String ppBase = input.getParentFile().getParent();
		String outputPath = StringUtils.join(new String[] { ppBase, "previews", contentId }, File.separator);

		File inputFile = new File(inputPath);
		File outputFile = new File(outputPath);

		try {
			// connect to an OpenOffice.org instance running on port 8100
			OpenOfficeConnection connection = new SocketOpenOfficeConnection(8100);
			connection.connect();

			// convert
			DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
			converter.convert(inputFile, outputFile);
			previewPaths.add(outputPath);
			log.info("Converted {} to {}", inputFile.getName(), outputFile.getName());

			// close the connection
			connection.disconnect();
		}
		catch (ConnectException e){
			throw new ProcessingException("Error connecting to the OOo document converter.", e);
		}
		return previewPaths;
	}
}
