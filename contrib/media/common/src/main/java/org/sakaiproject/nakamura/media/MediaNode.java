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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.nakamura.api.files.FilesConstants;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


class MediaNode {

  private static final Logger LOGGER = LoggerFactory
    .getLogger(MediaNode.class);


  private ContentManager contentManager;
  private String path;

  protected MediaNode(ContentManager cm, String path) {
    this.contentManager = cm;
    this.path = path;
  }


  public static MediaNode get(String parent, ContentManager cm, boolean create)
    throws StorageClientException, AccessDeniedException {
    String mediaNodePath = parent + "-medianode";

    Content obj = cm.get(mediaNodePath);

    if (obj == null) {
      if (create) {
        obj = new Content(mediaNodePath, new HashMap<String, Object>());
        cm.update(obj, true);

        cm.update(new Content(mediaNodePath + "/replicationStatus",
                new HashMap<String, Object>()),
            true);
      } else {
        return null;
      }
    }

    return new MediaNode(cm, mediaNodePath);
  }


  public void recordVersion(Version version) throws AccessDeniedException, StorageClientException {
    Content replicationStatus = getReplicationStatus(version);

    replicationStatus.setProperty("metadataVersion", version.metadataVersion());

    contentManager.update(replicationStatus);
  }

  public List<String> getMediaIds() throws AccessDeniedException, StorageClientException {
    String repStatusPath = path + "/replicationStatus";

    Iterator<Content> repStatusChildren = contentManager.listChildren(repStatusPath);

    List<String> mediaIds = Lists.newArrayList();
    while(repStatusChildren.hasNext()) {
      Content replicationStatus = repStatusChildren.next();
      mediaIds.add((String) replicationStatus.getProperty("bodyMediaId"));
    }
    return mediaIds;
  }

  public String getMediaId(Version version) throws AccessDeniedException, StorageClientException {
    Content replicationStatus = getReplicationStatus(version);
    return (String)replicationStatus.getProperty("bodyMediaId");
  }


  public boolean isReadyToPlay(Version version) throws AccessDeniedException, StorageClientException {
    Content replicationStatus = getReplicationStatus(version);
    return replicationStatus.getProperty("readyToPlay") != null;
  }


  public void setReadyToPlay(Version version) throws AccessDeniedException, StorageClientException {
    Content replicationStatus = getReplicationStatus(version);
    replicationStatus.setProperty("readyToPlay", "Y");
    contentManager.update(replicationStatus);
  }


  public void storeMediaId(Version version, String mediaId) throws AccessDeniedException, StorageClientException {
    Content replicationStatus = getReplicationStatus(version);

    replicationStatus.setProperty("bodyMediaId", mediaId);
    replicationStatus.setProperty("bodyUploaded", "Y");

    replicationStatus.setProperty("metadataVersion", version.metadataVersion());

    contentManager.update(replicationStatus);
  }
  
  public void storeMimeType(Version version, String mimeType) throws AccessDeniedException, StorageClientException {
    Content replicationStatus = getReplicationStatus(version);

    replicationStatus.setProperty(FilesConstants.POOLED_CONTENT_MIMETYPE, mimeType);
    replicationStatus.setProperty("bodyUploaded", "Y");

    replicationStatus.setProperty("metadataVersion", version.metadataVersion());

    contentManager.update(replicationStatus);
  }


  private Content getReplicationStatus(Version version) throws StorageClientException, AccessDeniedException {
    String mypath = path + "/replicationStatus/" + version.getVersionId();

    Content replicationStatus = contentManager.get(mypath);

    if (replicationStatus == null) {
      replicationStatus = new Content(mypath, new HashMap<String, Object>());
      contentManager.update(replicationStatus, true);
    }

    return replicationStatus;
  }


  public boolean isBodyUploaded(Version version) throws StorageClientException, AccessDeniedException {
    Content replicationStatus = getReplicationStatus(version);

    return ("Y".equals(replicationStatus.getProperty("bodyUploaded")));
  }


  public boolean isMetadataUpToDate(Version version) throws StorageClientException, AccessDeniedException {
    Content replicationStatus = getReplicationStatus(version);

    return (version.metadataVersion().equals(replicationStatus.getProperty("metadataVersion")));
  }


  public void storeProperties(Version version, Map<String, Object> props) throws StorageClientException, AccessDeniedException {
    Content replicationStatus = getReplicationStatus(version);
    for (Entry<String, Object> entry: props.entrySet()){
      replicationStatus.setProperty(entry.getKey(), entry.getValue());
    }
    contentManager.update(replicationStatus);
  }
}
