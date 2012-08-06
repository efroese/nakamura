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
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.office.ExternalOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.sakaiproject.nakamura.preview.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert documents to PDFs using the OOo JODConverter.
 */
@Service(PDFConverter.class)
@Component(metatype=true, immediate = true)
public class PDFConverter {

  private Logger log = LoggerFactory.getLogger(PDFConverter.class);

  @Property
  public static final String PROP_JOD_PORT = "jod.port";
  public static final int DEFAULT_JOD_PORT = 8100;
  protected int port;

  @Property
  public static final String PROP_JOD_TIMEOUT = "jod.processing.timeout";
  public static final int DEFAULT_JOD_TIMEOUT = 300; // seconds
  protected int timeout;

  protected ExternalOfficeManagerConfiguration configuration;
  protected OfficeManager officeManager;

  public PDFConverter(){ }

  public PDFConverter(int port, int timeout){
    this.port = port;
    this.timeout = timeout;
  }

  @Activate
  @Modified
  public void modified(Map<String,Object> props){
    port = PropertiesUtil.toInteger(props.get(PROP_JOD_PORT), DEFAULT_JOD_PORT);
    timeout = PropertiesUtil.toInteger(props.get(PROP_JOD_TIMEOUT), DEFAULT_JOD_TIMEOUT);
    deactivate();
    initOfficeManager();
  }

  public void initOfficeManager() {
    configuration = new ExternalOfficeManagerConfiguration();
    configuration.setPortNumber(port);
    officeManager = configuration.buildOfficeManager();
    officeManager.start();
  }

  @Deactivate
  public void deactivate(){
    try {
      if (officeManager != null){
        officeManager.stop();
      }
    }
    catch (Exception e){
      officeManager = null;
    }
  }

  /**
   * Convert a document to a PDF using the OOo processor.
   * @param inputPath The path to the document.
   * @param outputPath Where to write it
   * @throws ProcessingException
   */
  public void process(String inputPath, String outputPath) throws ProcessingException {
    try {
      OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager, new DefaultDocumentFormatRegistry());
      log.info("Converting {} to {}", inputPath, outputPath);
      converter.convert(new File(inputPath), new File(outputPath));
    }
    catch (Exception e){
      throw new ProcessingException("Error connecting to the JOD OOo document converter.", e);
    }
  }

  public void setPort(int port){
    this.port = port;
  }
}
