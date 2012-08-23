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
package org.sakaiproject.nakamura.media.brightcove;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentException;
import org.sakaiproject.nakamura.api.files.FilesConstants;
import org.sakaiproject.nakamura.api.media.MediaMetadata;
import org.sakaiproject.nakamura.api.media.MediaService;
import org.sakaiproject.nakamura.api.media.MediaServiceException;
import org.sakaiproject.nakamura.api.media.MediaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;


@Component(metatype = true, policy = ConfigurationPolicy.REQUIRE)
@Service
public class BrightCoveMediaService implements MediaService {

  public static final String X_MEDIA_BRIGHTCOVE = "application/x-media-brightcove";

  static final String OBJECT_CLASS_DEFAULT = "BrightcoveExperience";
  @Property(value = OBJECT_CLASS_DEFAULT)
  public static final String OBJECT_CLASS = "myExperience.class";

  static final String BG_COLOR_DEFAULT = "#FFFFFF";
  @Property(value = BG_COLOR_DEFAULT)
  public static final String BG_COLOR = "bgcolor";

  static final String WIDTH_DEFAULT = "500";
  @Property(value = WIDTH_DEFAULT)
  public static final String WIDTH = "width";

  static final String HEIGHT_DEFAULT = "470";
  @Property(value = HEIGHT_DEFAULT)
  public static final String HEIGHT = "height";

  @Property
  public static final String PLAYER_ID = "playerID";

  @Property
  public static final String PLAYER_KEY = "playerKey";

  static final boolean IS_VID_DEFAULT = true;
  @Property(boolValue = IS_VID_DEFAULT)
  public static final String IS_VID = "isVid";

  static final boolean DYNAMIC_STREAMING_DEFAULT = true;
  @Property(boolValue = DYNAMIC_STREAMING_DEFAULT)
  public static final String DYNAMIC_STREAMING = "dynamicStreaming";

  static final String W_MODE_DEFAULT = "opaque";
  @Property(value = W_MODE_DEFAULT)
  public static final String W_MODE = "wmode";

  private static final Logger LOG = LoggerFactory.getLogger(BrightCoveMediaService.class);

  @Property
  public static final String READ_TOKEN = "readToken";

  @Property(cardinality = Integer.MAX_VALUE)
  public static final String WRITE_TOKENS = "writeTokens";

  static final String BASE_URL_DEFAULT = "http://api.brightcove.com/services";
  @Property(value = BASE_URL_DEFAULT)
  public static final String BASE_URL = "baseUrl";

  @Property(value = { "3gp", "3g2", "h261", "h263", "h264", "jpgv", "jpm", "jpgm", "mj2", "mjp2", "mp4", "mp4v", "mpg4", "mpeg", "mpg", "mpe", "m1v", "m2v", "ogv", "qt", "mov", "uvh", "uvvh", "uvm", "uvvm", "uvp", "uvvp", "uvs", "uvvs", "uvv", "uvvv", "dvb", "fvt", "mxu", "m4u", "pyv", "uvu", "uvvu", "viv", "webm", "f4v", "fli", "flv", "m4v", "mkv", "mk3d", "mks", "mng", "asf", "asx", "vob", "wm", "wmv", "wmx", "wvx", "avi", "movie", "smv" })
  public static final String VIDEO_EXTENSIONS = "supportedVideoExtensionsList";

  private static final String REQ_MSG_TMPL = "'%s' required to communicate with BrightCove";

  private static final String[] PLAYER_SCRIPTS = new String[] {
    "http://admin.brightcove.com/js/BrightcoveExperiences.js",
    "http://admin.brightcove.com/js/APIModules_all.js"
  };

  private static final String OBJECT_EL_TMPL = "<object id=\"myExperience%s\" class=\"%s\">" +
    "  <param name=\"bgcolor\" value=\"%s\" />" +
    "  <param name=\"width\" value=\"%s\" />" +
    "  <param name=\"height\" value=\"%s\" />" +
    "  <param name=\"playerID\" value=\"%s\" />" +
    "  <param name=\"playerKey\" value=\"%s\" />" +
    "  <param name=\"isVid\" value=\"%s\" />" +
    "  <param name=\"dynamicStreaming\" value=\"%s\" />" +
    "  <param name=\"wmode\" value=\"%s\" />" +
    "  <param name=\"@videoPlayer\" value=\"%s\" />" +
    "</object>";


  String readToken;
  String[] writeTokens;
  String supportedVideoExtensionsList;
  String baseUrl;
  String libraryUrl;
  String postUrl;
  String bgcolor;
  String dynamicStreaming;
  String height;
  String isVid;
  String objectClass;
  String playerID;
  String playerKey;
  String width;
  String wMode;

  private HashSet<String> supportedVideoExtensions = new HashSet<String>();

  private BlockingQueue<String> writeTokenPool = new LinkedBlockingQueue<String>();


  public BrightCoveMediaService() {
  }

  @Activate
  @Modified
  protected void activate(Map<?, ?> props) {
    // ---------- required properties ------------------------------------------
    readToken = PropertiesUtil.toString(props.get(READ_TOKEN), null);
    // require readToken
    if (StringUtils.isBlank(readToken)) {
      throw new ComponentException(String.format(REQ_MSG_TMPL, "readToken"));
    }

    writeTokens = PropertiesUtil.toStringArray(props.get(WRITE_TOKENS), new String[] {});
    // require at least one write token
    if (writeTokens.length == 0) {
      throw new ComponentException(String.format(REQ_MSG_TMPL, "writeTokens"));
    }

    writeTokenPool.clear();
    for (String writeToken : writeTokens) {
      writeTokenPool.add(writeToken);
    }


    playerID = PropertiesUtil.toString(props.get(PLAYER_ID), null);
    // require playerID
    if (StringUtils.isBlank(playerID)) {
      throw new ComponentException(String.format(REQ_MSG_TMPL, "playerID"));
    }

    playerKey = PropertiesUtil.toString(props.get(PLAYER_KEY), null);
    // require playerKey
    if (StringUtils.isBlank(playerKey)) {
      throw new ComponentException(String.format(REQ_MSG_TMPL, "playerKey"));
    }

    baseUrl = PropertiesUtil.toString(props.get(BASE_URL), BASE_URL_DEFAULT);
    // require baseUrl
    if (StringUtils.isBlank(baseUrl)) {
      throw new ComponentException(String.format(REQ_MSG_TMPL, "baseUrl"));
    }
    libraryUrl = String.format("%s/library", baseUrl);
    postUrl = String.format("%s/post", baseUrl);

    // ---------- optional properties ------------------------------------------
    supportedVideoExtensions.clear();
    for (String ext : PropertiesUtil.toStringArray(props.get(VIDEO_EXTENSIONS), new String[] {}))  {
      supportedVideoExtensions.add(ext.toLowerCase());
    }

    bgcolor = PropertiesUtil.toString(props.get(BG_COLOR), BG_COLOR_DEFAULT);
    dynamicStreaming = Boolean.toString(PropertiesUtil.toBoolean(props.get(DYNAMIC_STREAMING), DYNAMIC_STREAMING_DEFAULT));
    height = PropertiesUtil.toString(props.get(HEIGHT), HEIGHT_DEFAULT);
    isVid = Boolean.toString(PropertiesUtil.toBoolean(props.get(IS_VID), IS_VID_DEFAULT));
    objectClass = PropertiesUtil.toString(props.get(OBJECT_CLASS), OBJECT_CLASS_DEFAULT);
    width = PropertiesUtil.toString(props.get(WIDTH), WIDTH_DEFAULT);
    wMode = PropertiesUtil.toString(props.get(W_MODE), W_MODE_DEFAULT);
  }


  // --------------- Write token coordination -----------------------------------
  
  abstract class WriteTask<E> {
    abstract E handle(String writeToken) throws MediaServiceException;

    public E run() throws MediaServiceException {
      String writeToken;

      try {
        LOG.debug("Acquiring write token");
        writeToken = writeTokenPool.take();
        LOG.debug("Acquired token '{}'", writeToken);
      } catch (InterruptedException e) {
        throw new MediaServiceException("Got InterruptedException while acquiring write token", e);
      }

      try {
        return handle(writeToken);
      } finally {
        LOG.debug("Returning token '{}' to pool", writeToken);
        writeTokenPool.add(writeToken);
        LOG.debug("Token '{}' returned", writeToken);
      }
    }
  }


  // --------------- MediaService interface -----------------------------------
  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#createMedia(java.io.File,
   *      java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public Map<String, Object> createMedia(File mediaFile, MediaMetadata metadata) throws MediaServiceException {
    String response = sendMedia(mediaFile, metadata);
    LOG.debug(response);
    Map<String, Object> props = new HashMap<String, Object>(2);
    props.put(FilesConstants.POOLED_CONTENT_MIMETYPE, X_MEDIA_BRIGHTCOVE);
    props.put("mediaId", response);
    return props;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#updateMedia(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public String updateMedia(MediaMetadata metadata)
    throws MediaServiceException {
    String response = sendMedia(null, metadata);
    LOG.debug(response);
    return response;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#deleteMedia(java.lang.String)
   */
  @Override
  public void deleteMedia(final String id) throws MediaServiceException {
    new WriteTask<MediaStatus>() {
      public MediaStatus handle(String writeToken) throws MediaServiceException {
        PostMethod post = null;

        try {
          JSONObject json = new JSONObject()
            .put("method", "delete_video")
            .put("params", new JSONObject()
                .put("token", writeToken)
                .put("video_id", id));
          // Define the url to the api
          post = new PostMethod(postUrl);
          Part[] parts = { new StringPart("JSON-RPC", json.toString()) };
          post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

          HttpClient client = new HttpClient();
          int returnCode = client.executeMethod(post);
          if (returnCode != 200) {
            throw new MediaServiceException("Unexpected return code: " + returnCode);
          }
          return null;
        } catch (JSONException e) {
          LOG.error(e.getMessage(), e);
          throw new MediaServiceException(e.getMessage(), e);
        } catch (IOException e) {
          throw new MediaServiceException(e.getMessage(), e);
        } finally {
          if (post != null) {
            post.releaseConnection();
          }
        }
      }
    }.run();
  }


  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#getStatus(java.lang.String)
   */
  @Override
  public MediaStatus getStatus(final String id) throws MediaServiceException {
    return new WriteTask<MediaStatus>() {
      public MediaStatus handle (String writeToken) throws MediaServiceException {

        PostMethod post = null;

        MediaStatus result;

        try {
          JSONObject json = new JSONObject()
            .put("method", "get_upload_status")
            .put("params", new JSONObject()
                .put("token", writeToken)
                .put("video_id", id));
          // Define the url to the api
          post = new PostMethod(postUrl);
          Part[] parts = { new StringPart("JSON-RPC", json.toString()) };
          post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

          HttpClient client = new HttpClient();
          int returnCode = client.executeMethod(post);

          JSONObject response = new JSONObject(post.getResponseBodyAsString());
          final String status = response.getString("result");

          LOG.debug("Status from Brightcove: {}", status);

          result = new MediaStatus() {
              public boolean isReady() {
                return ("COMPLETE".equals(status));
              }

              public boolean isProcessing() {
                return ("PROCESSING".equals(status) || "UPLOADING".equals(status));
              }

              public boolean isError() {
                return "ERROR".equals(status);
              }
            };

        } catch (JSONException e) {
          LOG.error(e.getMessage(), e);
          throw new MediaServiceException(e.getMessage(), e);
        } catch (IOException e) {
          throw new MediaServiceException(e.getMessage(), e);
        } finally {
          if (post != null) {
            post.releaseConnection();
          }
        }

        return result;
      }
    }.run();
  }

  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#getPlayerFragment(java.lang.String)
   */
  @Override
  public String getPlayerFragment(String id) {
    return String.format(OBJECT_EL_TMPL, id, objectClass, bgcolor,
        width, height, playerID, playerKey, isVid, dynamicStreaming, wMode, id);
  }


  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#getPlayerJSUrls(java.lang.String)
   */
  @Override
  public String[] getPlayerJSUrls(String id) {
    return PLAYER_SCRIPTS;
  }


  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#getPlayerInitJS(java.lang.String)
   */
  @Override
  public String getPlayerInitJS(String id) {
    return "brightcove.createExperiences();";
  }


  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#acceptsMimeType(String)
   */
  @Override
  public boolean acceptsFileType(String mimeType, String extension) {
    return X_MEDIA_BRIGHTCOVE.equals(mimeType) ||
        (mimeType.startsWith("video/") && supportedVideoExtensions.contains(extension));
  }


  private String sendMedia(final File mediaFile, final MediaMetadata metadata)
      throws MediaServiceException {
    if (metadata.getId() == null && mediaFile == null) {
      throw new IllegalArgumentException("Must supply 'id' or 'mediaFile'");
    }

    return new WriteTask<String>() {
      public String handle (String writeToken) throws MediaServiceException {
        PostMethod post = null;
        try {
          /*
           * Assemble the JSON params
           */
          JSONObject media = new JSONObject()
            .put("name", metadata.getTitle())
            .put("shortDescription", metadata.getDescription())
            .put("tags", Arrays.asList(metadata.getTags()));

          String method = "update_video";

          if (mediaFile != null) {
            method = "create_video";
          } else {
            media.put("id", metadata.getId());
          }

          JSONObject json = new JSONObject()
            .put("method", method)
            .put("params", new JSONObject()
                .put("token", writeToken)
                .put("create_multiple_renditions", true)
                .put("preserve_source_rendition", true)
                .put("encode_to", "MP4")
                .put("video", media));

          Part[] parts;
          if (mediaFile != null) {
            parts = new Part[] {
              new StringPart("JSON-RPC", json.toString()),
              new FilePart("fileData",
                  new PartSource () {
                    public InputStream createInputStream() {
                      try {
                        return new FileInputStream(mediaFile);
                      } catch (FileNotFoundException e) {
                        throw new RuntimeException("Couldn't find media file: " + mediaFile);
                      }
                    }

                    public String getFileName() {
                      if (metadata.getExtension() != null) {
                        return metadata.getTitle() + "." + metadata.getExtension();
                      } else {
                        return metadata.getTitle();
                      }
                    }

                    public long getLength() {
                      return mediaFile.length();
                    }
                  })
            };
          } else {
            parts = new Part[] { new StringPart("JSON-RPC", json.toString()) };
          }

          HttpClient client = new HttpClient();

          post = new PostMethod(postUrl);
          post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

          int returnCode = client.executeMethod(post);

          String response = post.getResponseBodyAsString();

          String msg = String.format("Sent: %s, Posted media information [%s]: %s", new Object[] {
                json.toString(), returnCode, response });
          LOG.debug(msg);

          JSONObject responseJSON = new JSONObject(response);

          JSONObject error = responseJSON.has("error") ? responseJSON.optJSONObject("error") : null;

          if (error != null) {
            throw new MediaServiceException(error.getString("name") + ": " + error.getString("message"));
          }

          if (mediaFile != null) {
            // This was an upload.  Return the new ID
            return String.valueOf(responseJSON.getLong("result"));
          } else {
            // An update.  Return the old ID
            return metadata.getId();
          }

        } catch (JSONException e) {
          throw new MediaServiceException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
          throw new MediaServiceException(e.getMessage(), e);
        } catch (HttpException e) {
          throw new MediaServiceException(e.getMessage(), e);
        } catch (IOException e) {
          throw new MediaServiceException(e.getMessage(), e);
        } finally {
          if (post != null) {
            post.releaseConnection();
          }
        }
      }
    }.run();
  }
}
