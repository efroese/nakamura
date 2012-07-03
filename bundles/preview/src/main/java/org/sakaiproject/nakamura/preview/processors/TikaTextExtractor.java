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
import java.io.FileInputStream;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

/**
 * Extract the body text from a variety of documents using Tika.
 */
@Service(TikaTextExtractor.class)
@Component(immediate = true)
public class TikaTextExtractor {

  private static final Logger log = LoggerFactory.getLogger(TikaTextExtractor.class);

  /**
   * Extract the text from the body content of a document using Tika
   * @param inputPath path o the file
   * @return a String representing the body content
   */
  public String getText(String inputPath){
    Parser parser = new AutoDetectParser();
    ContentHandler handler = new BodyContentHandler();
    try {
      parser.parse(new FileInputStream(new File(inputPath)), handler, new Metadata(), new ParseContext());
    } catch (Exception e) {
      log.error("Error while trying to extract text from {}", inputPath);
    }
    String text = handler.toString();
    log.debug("Extracted text from {} : {}", inputPath, text);
    return text;
  }
}
