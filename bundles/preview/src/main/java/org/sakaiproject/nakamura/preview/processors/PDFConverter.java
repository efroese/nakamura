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
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.sakaiproject.nakamura.preview.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

/**
 * Convert documents to PDFs using the OOo JODConverter.
 */
@Service(PDFConverter.class)
@Component(metatype=true, immediate = true)
public class PDFConverter {

  private Logger log = LoggerFactory.getLogger(PDFConverter.class);

  OpenOfficeConnection connection = null;

  @Property
  public static final String PROP_JOD_PORT = "jod.port";
  public static final int DEFAULT_JOD_PORT = 8100;
  protected int port;

  @Activate
  @Modified
  public void modified(Map<String,Object> props){
    port = PropertiesUtil.toInteger(props.get(PROP_JOD_PORT), DEFAULT_JOD_PORT);
  }

  @Deactivate
  public void deactivate(){
    if (connection == null){
      return;
    }
    connection.disconnect();
    connection = null;
  }

  public OpenOfficeConnection getConnection() throws ConnectException{
    if (connection == null){
      connection = new SocketOpenOfficeConnection(port);
    }
    if (!connection.isConnected()){
      connection.connect();
    }
    return connection;
  }

  /**
   * Convert a document to a PDF using the OOo processor.
   * @param inputPath The path to the document.
   * @param outputPath Where to write it
   * @throws ProcessingException
   */
  public void process(String inputPath, String outputPath) throws ProcessingException {
    try {
      log.info("Converting {} to {}", inputPath, outputPath);
      DocumentConverter converter = new OpenOfficeDocumentConverter(getConnection());
      converter.convert(new File(inputPath), new File(outputPath));
    }
    catch (ConnectException e){
      throw new ProcessingException("Error connecting to the JOD OOo document converter.", e);
    }
  }
}
