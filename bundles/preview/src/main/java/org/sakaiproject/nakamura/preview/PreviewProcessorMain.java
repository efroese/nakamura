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

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewProcessorMain {
	
	private static Logger log = LoggerFactory.getLogger(PreviewProcessorMain.class);

	/**
	 * Load up the mime.types and ignore.types files
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static Set<String> loadResourceSet(String filename) throws IOException{
		ClassLoader cl = PreviewProcessorMain.class.getClassLoader();
		Set<String> resource = new HashSet<String>();
		for (String line: (List<String>)IOUtils.readLines(cl.getResourceAsStream(filename))){
			if (!line.startsWith("#") && !line.trim().equals("")){
				resource.add(line.trim());
			}
		}
		log.trace("Loaded resource {}: {}", filename, resource);
		return resource;
	}

	public static void main(String[] args) throws Exception{
		
		if (args.length < 3){
			System.out.println("usage: java -jar jarpath http://localhost:8080 adminpass /var/sakaioae/preview_processor/");
			return;
		}
		String server = args[0];
		String password = args[1];
		String basePath = args[2];
		
		PreviewProcessorImpl pp = new PreviewProcessorImpl();
		pp.server = new URL(server);
		pp.password = password;
		pp.basePath = basePath;
		pp.ignoreTypes = loadResourceSet("ignore.types");
		pp.mimeTypes = loadResourceSet("mime.types");
		pp.contentFetcher = new SearchContentFetcher();
		pp.nakamura = new NakamuraFacade(server, password);
		pp.process();
	}
}
