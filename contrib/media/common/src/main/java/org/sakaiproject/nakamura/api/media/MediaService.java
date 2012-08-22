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
package org.sakaiproject.nakamura.api.media;

import java.io.File;
import java.io.IOException;

public interface MediaService {
  /**
   * Create media with supplied metadata.
   *
   * @param media
   * @param title
   * @param description
   * @param extension
   * @param tags
   * @return
   * @throws MediaServiceException
   */
  String createMedia(File media, MediaMetadata metadata) throws MediaServiceException;

  /**
   * Update the metadata for a media.
   *
   * @param id
   * @param title
   * @param description
   * @param tags
   * @return
   * @throws MediaServiceException
   */
  String updateMedia(MediaMetadata metadata) throws MediaServiceException;

  /**
   * Delete media.
   *
   * @param id
   */
  void deleteMedia(String id) throws MediaServiceException;

  /**
   * Get the status of a media.
   *
   * @param id
   * @return A MediaStatus object representing the media's state
   * @throws MediaServiceException
   */
  MediaStatus getStatus(String id) throws MediaServiceException, IOException;

  /**
   * Return the HTML snippet for a player that will play the given video ID.
   * @param id The ID of the video in the media service
   * @return A string of HTML that will be inserted into the UI's DOM
   */
  String getPlayerFragment(String id);


  /**
   * Return a list of URLs of the JavaScript files that this player needs to have loaded
   * @param id The ID of the video in the media service
   * @return A list of URLs
   */
  String[] getPlayerJSUrls(String id);


  /**
   * Return a fragment of JavaScript that will begin a video playing
   * @param id The ID of the video in the media service
   * @return A JavaScript string
   */
  String getPlayerInitJS(String id);


  /**
   * Check whether the media service can handle a certain mime type and
   * extension.
   * @param mimeType A mime type like "video/mpeg"
   * @param extension A file extension like "mpg"
   * @return True if this media service supports a given mime type. False
   *        otherwise.
   */
  boolean acceptsFileType(String mimeType, String extension);

}
