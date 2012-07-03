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

import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.sakaiproject.nakamura.preview.processors.ImageProcessor;
import org.sakaiproject.nakamura.preview.processors.PDFBoxProcessor;
import org.sakaiproject.nakamura.preview.processors.TikaTextExtractor;
import org.sakaiproject.nakamura.preview.util.RemoteServerUtil;
import org.sakaiproject.nakamura.termextract.TermExtractorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class PreviewProcessorMain {

  private static final String DEFAULT_COUNT = "1";

  private static Logger log = LoggerFactory
  .getLogger(PreviewProcessorMain.class);

  public static void main(String[] args) throws Exception {

    Options options = new Options();
    options.addOption("s", "server", true, "OAE URL: http://localhost:8080");
    options.addOption("c", "content", true,
    "OAE Content URL: http://localhost:8082");
    options.addOption("u", "user", true, "OAE admin user");
    options.addOption("p", "password", true, "OAE admin PASSWORD");
    options.addOption("d", "directory", true,
    "Working DIRECTORY for downloads, previews, logs");
    options.addOption("n", "count", true, "Run the processing COUNT times");
    options.addOption("t", "tagging", false, "Force tagging of all documents.");

    CommandLineParser parser = new GnuParser();
    CommandLine cmd = parser.parse(options, args);

    String usage = "java -jar preview.jar -s http://localhost:8080 -c http://localhost:8082 -u admin -p admin -d /var/oae/pp [-i 1] [-n 1]";
    if (!cmd.hasOption("server") || !cmd.hasOption("content")
        || !cmd.hasOption("password") || !cmd.hasOption("directory")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(usage, options);
      System.exit(1);
    }

    int count = Integer.parseInt(cmd.getOptionValue("count", DEFAULT_COUNT));

    PreviewProcessorImpl pp = new PreviewProcessorImpl();
    Builder<String, Object> props =  ImmutableMap.builder();

    props.put(PreviewProcessorImpl.PROP_REMOTE_SERVER_URL, cmd.getOptionValue("server"));
    props.put(PreviewProcessorImpl.PROP_REMOTE_CONTENT_SERVER_URL, cmd.getOptionValue("content"));
    props.put(PreviewProcessorImpl.PROP_REMOTE_SERVER_USER, cmd.getOptionValue("user"));
    props.put(PreviewProcessorImpl.PROP_REMOTE_SERVER_PASSWORD, cmd.getOptionValue("password"));
    props.put(PreviewProcessorImpl.PROP_BASEPATH, cmd.getOptionValue("directory"));
    props.put(PreviewProcessorImpl.PROP_MAX_TAGS, PreviewProcessorImpl.DEFAULT_MAX_TAGS);
    props.put(PreviewProcessorImpl.PROP_FORCE_TAGGING, cmd.getOptionValue("tagging"));
    pp.modified(props.build());

    pp.remoteServerUrl = new URL(cmd.getOptionValue("server"));
    pp.remoteContentServerUrl = new URL(cmd.getOptionValue("content"));
    pp.contentFetcher = new SearchContentFetcher();
    pp.remoteServer = new RemoteServerUtil(cmd.getOptionValue("server"), cmd.getOptionValue("password"));

    pp.thumbnailGenerator = new ImageProcessor();
    pp.pdfSplitter = new PDFBoxProcessor();
    pp.textExtractor = new TikaTextExtractor();
    pp.termExtractor = new TermExtractorImpl();

    if (cmd.hasOption("tagging")){
      log.info("Tagging all content regardless of user preferences!");
    }

    try {
      while (count >= 0) {
        pp.process();
        count--;
      }
      System.exit(0);
    }
    catch (Exception e){
      log.error("The preview processor threw an exception {}", e);
    }
    finally {
      System.exit(1);
    }
  }
}
