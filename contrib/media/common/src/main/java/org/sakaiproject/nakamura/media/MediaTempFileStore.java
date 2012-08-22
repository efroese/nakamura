/*
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
 * specific language governing permissions and limitations
 * under the License.
 */
package org.sakaiproject.nakamura.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager class to handle reading, writing, and destroying temporary media
 * content bodies.
 */
public class MediaTempFileStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaTempFileStore.class); 

  File storeDirectory;

  public MediaTempFileStore(String tempDir) {
    this.storeDirectory = new File(tempDir, "oae-media-store");

    storeDirectory.mkdirs();
  }


  synchronized File storageFor(String path) throws IOException {
    File dir = new File(storeDirectory, path);

    dir.mkdirs();

    long version = System.currentTimeMillis();
    long rand = Math.round(Math.random() * 1000);

    File f = new File(dir, String.format("%020d%d.tmp", version, rand));
    f.createNewFile();

    return f;
  }


  public String store(InputStream inputStream, String path)
    throws FileNotFoundException, IOException {

    File f = storageFor(path);

    FileOutputStream out = new FileOutputStream(f);

    IOUtils.copy(inputStream, out);

    out.close();

    File newFile = new File(f.getPath().replaceAll("\\.tmp$", ""));

    if (f.renameTo(newFile)) {
      return newFile.getName();
    } else {
      throw new IOException("Failed to rename file: " + f);
    }
  }


  synchronized public File getFile(String path, String versionId) {

    File dir = new File(storeDirectory, path);
    File tempFile = new File(dir, versionId);

    if (!tempFile.exists()) {
      return null;
    }

    return tempFile;
  }
  

  synchronized void completed(String path, String versionId) {
    File file = getFile(path, versionId);

    if (file == null) {
      LOGGER.info("Couldn't find a file associated with version '{}' of path '{}'",
          versionId, path);
      return;
    }

    LOGGER.info("Removing temp file associated with version '{}' of path '{}'",
        versionId, path);

    file.delete();

    File parent = file.getParentFile();

    if (parent.listFiles().length == 0) {
      LOGGER.info("Cleaning up directory for path '{}' (no more files)", path);

      // Clean up
      parent.delete();
    }
  }
}
