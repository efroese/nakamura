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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.sakaiproject.nakamura.preview.util.RemoteServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewProcessorMain {
	
	private static final String DEFAULT_INTERVAL = "1";
	private static final String DEFAULT_COUNT = "1";

  private static Logger log = LoggerFactory.getLogger(PreviewProcessorMain.class);

	@SuppressWarnings("unchecked")
	private static Set<String> loadResourceSet(String filename) throws IOException{
		ClassLoader cl = PreviewProcessorMain.class.getClassLoader();
		Set<String> resource = new HashSet<String>();
		for (String line: (List<String>)IOUtils.readLines(cl.getResourceAsStream(filename))){
			if (!line.startsWith("#") && !line.trim().equals("")){
				resource.add(line.trim());
			}
		}
		log.trace("Loaded resource {}: {} lines.", filename, resource.size());
		return resource;
	}

	public static void main(String[] args) throws Exception{

		Options options = new Options();
		options.addOption("s", "server", true, "OAE URL: http://localhost:8080");
		options.addOption("c", "content", true, "OAE Content URL: http://localhost:8082");
		options.addOption("p", "password", true, "OAE admin PASSWORD");
		options.addOption("d", "directory", true, "Working DIRECTORY for downloads, previews, logs");
		options.addOption("i", "interval", true, "Sleep for INTERVAL seconds between runs");
		options.addOption("n", "count", true, "Run the processing COUNT times");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(options, args);

		String usage = "java -jar preview.jar -s http://localhost:8080 -c http://localhost:8082 -p admin -d /var/oae/pp [-i 1] [-n 1]";
		if (!cmd.hasOption("server") || !cmd.hasOption("content")
		    || !cmd.hasOption("password") || !cmd.hasOption("directory")){
		  HelpFormatter formatter = new HelpFormatter();
		  formatter.printHelp(usage, options);
		  System.exit(1);
		}

		int interval = Integer.parseInt(cmd.getOptionValue("interval", DEFAULT_INTERVAL));
		int count = Integer.parseInt(cmd.getOptionValue("count", DEFAULT_COUNT));

		PreviewProcessorImpl pp = new PreviewProcessorImpl();
		pp.server = new URL(cmd.getOptionValue("server"));
		pp.contentServer = new URL(cmd.getOptionValue("content"));
		pp.password = cmd.getOptionValue("password");
		pp.basePath = cmd.getOptionValue("directory");
		pp.ignoreTypes = loadResourceSet("ignore.types");
		pp.mimeTypes = loadResourceSet("mime.types");
		pp.contentFetcher = new SearchContentFetcher();
		pp.nakamura = new RemoteServerUtil(cmd.getOptionValue("server"), cmd.getOptionValue("password"));
		pp.init();

		while (count-- >= 0){
		  pp.process();
		  Thread.sleep(interval * 1000);
		}
	}
}
