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
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.media;

/**
 *
 */
public class MediaMetadata {
  private String id; // mediaId in the remote store
  private String contentId; // OAE contentId in the remote store

  private String title;
  private String description;
  private String[] tags;
  private String user;
  private String extension;
  private String versionId;

  public MediaMetadata() {
  }

  public MediaMetadata(String id, String title, String description, String[] tags,
      String user, String contentId) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.tags = tags;
    this.user = user;
    this.contentId = contentId;
  }

  public MediaMetadata(String title, String description, String[] tags, String extension,
      String versionId, String user, String contentId) {
    this.title = title;
    this.description = description;
    this.tags = tags;
    this.extension = extension;
    this.versionId = versionId;
    this.user = user;
    this.contentId = contentId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getTags() {
    return tags;
  }

  public void setTags(String[] tags) {
    this.tags = tags;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(String versionId) {
    this.versionId = versionId;
  }
}
