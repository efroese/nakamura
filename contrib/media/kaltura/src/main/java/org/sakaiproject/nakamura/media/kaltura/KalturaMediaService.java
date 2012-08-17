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
package org.sakaiproject.nakamura.media.kaltura;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;

import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.media.MediaMetadata;
import org.sakaiproject.nakamura.api.media.MediaService;
import org.sakaiproject.nakamura.api.media.MediaServiceException;
import org.sakaiproject.nakamura.api.media.MediaStatus;
import org.sakaiproject.nakamura.lite.content.InternalContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaConfiguration;
import com.kaltura.client.enums.KalturaEntryStatus;
import com.kaltura.client.enums.KalturaMediaType;
import com.kaltura.client.enums.KalturaSessionType;
import com.kaltura.client.services.KalturaBaseEntryService;
import com.kaltura.client.services.KalturaSessionService;
import com.kaltura.client.types.KalturaBaseEntry;
import com.kaltura.client.types.KalturaMediaEntry;

/**
 *
 */
@Component(enabled = true, metatype = true, policy = ConfigurationPolicy.REQUIRE)
@Service
public class KalturaMediaService implements MediaService {

  private static final Logger LOG = LoggerFactory
      .getLogger(KalturaMediaService.class);

  public static final String KALTURA_MIMETYPE_VIDEO = "kaltura/video";
  public static final String KALTURA_MIMETYPE_AUDIO = "kaltura/audio";
  public static final String KALTURA_MIMETYPE_IMAGE = "kaltura/image";

  static final String WIDTH_DEFAULT = "500";
  @Property(value = WIDTH_DEFAULT)
  public static final String WIDTH = "width";
  String width;

  static final String HEIGHT_DEFAULT = "470";
  @Property(value = HEIGHT_DEFAULT)
  public static final String HEIGHT = "height";
  String height;

  @Property(intValue = 111, label = "Partner Id")
  private static final String KALTURA_PARTNER_ID = "kaltura.partnerid";
  int kalturaPartnerId;

  @Property(value = "setThisToYourKalturaSecret", label = "Secret")
  private static final String KALTURA_SECRET = "kaltura.secret";
  String kalturaSecret;

  @Property(value = "setThisToYourKalturaAdminSecret", label = "Admin Secret")
  private static final String KALTURA_ADMIN_SECRET = "kaltura.adminsecret";
  String kalturaAdminSecret;

  @Property(value = "http://www.kaltura.com", label = "Endpoint")
  private static final String KALTURA_ENDPOINT = "kaltura.endpoint";
  String kalturaEndpoint;

  @Property(value = "http://cdn.kaltura.com", label = "CDN")
  private static final String KALTURA_CDN = "kaltura.cdn";
  String kalturaCDN;

  public static final String DEFAULT_KALTURA_PLATER_AUDIO = "2158531";
  @Property(value = KalturaMediaService.DEFAULT_KALTURA_PLATER_AUDIO, label = "Player - Audio")
  private static final String KALTURA_PLAYER_AUDIO = "kaltura.player.audio";
  String kalturaPlayerIdAudio;

  public static final String DEFAULT_KALTURA_PLATER_VIDEO = "1522202";
  @Property(value = KalturaMediaService.DEFAULT_KALTURA_PLATER_VIDEO, label = "Player - Video View")
  private static final String KALTURA_PLAYER_VIEW = "kaltura.player.view";
  String kalturaPlayerIdView;

  KalturaConfiguration kalturaConfig;

  @Reference
  protected Repository repository;

  @Activate
  @Modified
  protected void activate(Map<?, ?> props) throws Exception {

    kalturaPartnerId = PropertiesUtil.toInteger(props.get(KALTURA_PARTNER_ID), -1);
    kalturaSecret = PropertiesUtil.toString(props.get(KALTURA_SECRET), null);
    kalturaAdminSecret = PropertiesUtil.toString(props.get(KALTURA_ADMIN_SECRET), null);
    kalturaEndpoint = PropertiesUtil.toString(props.get(KALTURA_ENDPOINT), null);
    kalturaCDN = PropertiesUtil.toString(props.get(KALTURA_CDN), null);

    for (String prop : new String[] { kalturaSecret, kalturaAdminSecret,
        kalturaEndpoint, kalturaCDN }) {
      if (StringUtils.isBlank(prop)) {
        throw new Exception(
            "Please check the kaltura configuration. Something is missing.");
      }
    }
    // Widget views
    kalturaPlayerIdAudio = PropertiesUtil.toString(KALTURA_PLAYER_AUDIO,
        DEFAULT_KALTURA_PLATER_AUDIO);
    kalturaPlayerIdView = PropertiesUtil.toString(KALTURA_PLAYER_VIEW,
        DEFAULT_KALTURA_PLATER_VIDEO);

    height = PropertiesUtil.toString(props.get(HEIGHT), HEIGHT_DEFAULT);
    width = PropertiesUtil.toString(props.get(WIDTH), WIDTH_DEFAULT);

    KalturaConfiguration kc = new KalturaConfiguration();
    kc.setPartnerId(kalturaPartnerId);
    kc.setSecret(kalturaSecret);
    kc.setAdminSecret(kalturaAdminSecret);
    kc.setEndpoint(kalturaEndpoint);
    kalturaConfig = kc;

    dumpServiceConfigToLog(props);

    // test out that the kc can initialize a session
    KalturaClient kalturaClient = makeKalturaClient("admin",
        KalturaSessionType.ADMIN, 10);
    if (kalturaClient == null || kalturaClient.getSessionId() == null) {
      throw new Exception("Failed to connect to kaltura server endpoint ("
          + kc.getEndpoint() + ") as admin");
    }
    kalturaClient = makeKalturaClient("admin", KalturaSessionType.USER, 10);
    if (kalturaClient == null || kalturaClient.getSessionId() == null) {
      throw new Exception("Failed to connect to kaltura server endpoint ("
          + kc.getEndpoint() + ") as user");
    }
    LOG.info(
        "Kaltura: Init complete: API version: {}, Connected to endpoint: {}",
        kalturaClient.getApiVersion(), kc.getEndpoint());
  }

  private void dumpServiceConfigToLog(Map<?, ?> properties) {
    String propsDump="";
    if (properties != null && LOG.isDebugEnabled()) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n Properties:\n");
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            sb.append("  * ");
            sb.append(entry.getKey());
            sb.append(" -> ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        propsDump = sb.toString();
    }
    LOG.info("\nKalturaService Configuration: START ---------\n"
            +" partnerId="+this.kalturaConfig.getPartnerId()+"\n"
            +" endPoint="+this.kalturaConfig.getEndpoint()+"\n"
            +" timeout="+this.kalturaConfig.getTimeout()+"\n"
            +" kalturaCDN="+this.kalturaCDN+"\n"
            +" kalturaPlayerIdView="+this.kalturaPlayerIdView+"\n"
            +" kalturaPlayerIdAudio="+this.kalturaPlayerIdAudio+"\n"
            +propsDump
            +"KalturaService Configuration: END ---------\n");
  }

  // ------ MediaService Interface Methods ------

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.media.MediaService#createMedia(java.io.File, org.sakaiproject.nakamura.api.media.MediaMetadata)
   */
  @Override
  public String createMedia(File media, MediaMetadata metadata)
      throws MediaServiceException {

    // exception if upload fails
    KalturaBaseEntry kbe;
    String mediaId = null;
    try {
      kbe = uploadItem(metadata.getUser(), media.getName(), media.length(),
          new FileInputStream(media), KalturaMediaType.VIDEO, metadata.getTitle(),
          metadata.getDescription(), StringUtils.join(metadata.getTags(), ","));

      if (kbe != null) {
        // item upload successful
        MediaItem mediaItem = new MediaItem(kbe, metadata.getUser());
        Map<String, Object> props = new HashMap<String, Object>(10);
        mediaId = mediaItem.getKalturaId();

        props.put("kaltura-updated", new Date().getTime());
        props.put("kaltura-id", mediaItem.getKalturaId());
        props.put("kaltura-thumbnail", mediaItem.getThumbnail());
        props.put("kaltura-download", mediaItem.getDownloadURL());
        // probably will be 0
        props.put("kaltura-duration", mediaItem.getDuration());
        props.put("kaltura-height", mediaItem.getHeight());
        props.put("kaltura-width", mediaItem.getWidth());
        props.put("kaltura-type", mediaItem.getType());
        String kalturaMimeType = KALTURA_MIMETYPE_VIDEO;
        if (MediaItem.TYPE_AUDIO.equals(mediaItem.getMediaType())) {
          kalturaMimeType = KALTURA_MIMETYPE_AUDIO;
        } else if (MediaItem.TYPE_IMAGE.equals(mediaItem.getMediaType())) {
          kalturaMimeType = KALTURA_MIMETYPE_IMAGE;
        }
        props.put(InternalContent.MIMETYPE_FIELD, kalturaMimeType);

        LOG.info(
            "Completed upload ({}) to Kaltura of file ({}) of type ({}) and created kalturaEntry ({})",
            new String[] { metadata.getTitle(), media.getName(), kalturaMimeType,
                mediaItem.getKalturaId() });

        updateContent(metadata.getContentId(), props); // exception if update fails
        // Map<String, Object> newProps = ...
        // dumpMapToLog(newProps, "newContentProperties");

      } else {
        // should we fail here if kaltura does not return a valid KBE? -AZ
        LOG.error("Response from kaltura was null.");
      }
      LOG.info("Kaltura file upload handler complete: {}", media.getName());

    } catch (FileNotFoundException e) {
      LOG.error("{} not found.", media.getAbsolutePath());
    }
    return mediaId;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.media.MediaService#updateMedia(org.sakaiproject.nakamura.api.media.MediaMetadata)
   */
  @Override
  public String updateMedia(MediaMetadata metadata) throws MediaServiceException {
    return null;
  }

  @Override
  public void deleteMedia(String id) throws MediaServiceException {
    if (id == null) {
      throw new IllegalArgumentException("id must not be null");
    }
    KalturaClient kc = getKalturaClient();
    if (kc != null) {
      try {
        KalturaBaseEntryService entryService = kc.getBaseEntryService();
        KalturaBaseEntry entry = getKalturaEntry(null, id, entryService);
        entryService.delete(entry.id);
      } catch (KalturaApiException e) {
        LOG.error("Unable to remove kaltura item ({} using session(oid={}, tid={}, ks={}):: {}",
            new String[]{ id, kc.toString(), Long.toString(Thread.currentThread().getId()),
            kc.getSessionId(), e.getMessage() });
        throw new MediaServiceException(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.media.MediaService#getStatus(java.lang.String)
   */
  @Override
  public MediaStatus getStatus(String id) throws MediaServiceException,
      IOException {
    if (id == null) {
      throw new IllegalArgumentException("id must not be null");
    }

    KalturaClient kc = getKalturaClient();

    if (kc != null) {
      try {
        KalturaBaseEntryService entryService = kc.getBaseEntryService();
        KalturaBaseEntry entry = getKalturaEntry(null, id, entryService);
        final KalturaEntryStatus kalturaStatus = entry.status;

        return new MediaStatus() {
          @Override
          public boolean isReady() {
            return kalturaStatus == KalturaEntryStatus.READY;
          }

          @Override
          public boolean isProcessing() {
            return kalturaStatus == KalturaEntryStatus.PENDING
                || kalturaStatus == KalturaEntryStatus.PRECONVERT;
          }

          @Override
          public boolean isError() {
            return kalturaStatus == KalturaEntryStatus.BLOCKED
                || kalturaStatus == KalturaEntryStatus.ERROR_CONVERTING
                || kalturaStatus == KalturaEntryStatus.ERROR_IMPORTING
                || kalturaStatus == KalturaEntryStatus.INFECTED;
          }
        };
      } catch (KalturaApiException e) {
        LOG.error(
            "Unable to query the status of kaltura item {} using session (oid={}, tid={}, ks={})::{}",
            new String[] { id, kc.toString(),
                Long.toString(Thread.currentThread().getId()),
                kc.getSessionId(), e.getMessage() });
        throw new MediaServiceException(e);
      }
    }
    // We're in an error state!
    return new MediaStatus() {

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public boolean isProcessing() {
        return false;
      }

      @Override
      public boolean isError() {
        return true;
      }
    };
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.media.MediaService#getPlayerFragment(java.lang.String)
   */
  @Override
  public String getPlayerFragment(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.media.MediaService#getPlayerJSUrls(java.lang.String)
   */
  @Override
  public String[] getPlayerJSUrls(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.media.MediaService#getPlayerInitJS(java.lang.String)
   */
  @Override
  public String getPlayerInitJS(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.media.MediaService#getMimeType()
   */
  @Override
  public String getMimeType() {
    return "application/x-media-kaltura";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sakaiproject.nakamura.api.media.MediaService#acceptsFileType(java.lang
   * .String, java.lang.String)
   */
  @Override
  public boolean acceptsFileType(String mimeType, String extension) {
    return mimeType.startsWith("video/");
  }

  // ------- Kaltura Methods -----

  /**
   * NOTE: this method will generate a new kaltura client, make sure you store
   * this into the {@link #kctl} threadlocal if you are generating it using this
   * method
   */
  private KalturaClient makeKalturaClient(String userKey,
      KalturaSessionType sessionType, int timeoutSecs) {
    // client is not threadsafe
    if (timeoutSecs <= 0) {
      timeoutSecs = 86400; // NOTE set to 24 hours by request of kaltura 60; //
                           // default to 60 seconds
    }
    KalturaClient kalturaClient = new KalturaClient(this.kalturaConfig);
    String secret = this.kalturaConfig.getSecret();
    if (KalturaSessionType.ADMIN.equals(sessionType)) {
      secret = this.kalturaConfig.getAdminSecret();
    }
    KalturaSessionService sessionService = kalturaClient.getSessionService();
    try {
      String sessionId = sessionService.start(secret, userKey, sessionType,
      // the edit is needed to fix an issue with kaltura servers
      this.kalturaConfig.getPartnerId(), timeoutSecs, "edit:*"); 
      kalturaClient.setSessionId(sessionId);
      LOG.debug("Created new kaltura client (oid={}, tid={}, ks={})",
          new String[] { kalturaClient.toString(), 
          Long.toString(Thread.currentThread().getId()), 
          kalturaClient.getSessionId()});
    } catch (KalturaApiException e) {
      // kalturaClient.setSessionId(null); // should we clear this?
      LOG.error("Unable to establish a kaltura session ({}, {}):: {}",
          new String[] { kalturaClient.toString(), kalturaClient.getSessionId(), e.getMessage() } );
    }
    return kalturaClient;
  }

  /*
   * NOTE: the KalturaClient is not even close to being threadsafe -AZ
   */
  ThreadLocal<KalturaClient> kctl = new ThreadLocal<KalturaClient>() {
    @Override
    protected KalturaClient initialValue() {
      return makeKalturaClient();
    };
  };

  /**
   * destroys the current kaltura client
   */
  public void clearKalturaClient() {
    kctl.remove();
  }

  /**
   * threadsafe method to get a kaltura client
   * 
   * @return the current kaltura client for this thread
   */
  public KalturaClient getKalturaClient() {
    return kctl.get();
  }

  /**
   * NOTE: this method will generate a new kaltura client using all defaults and
   * sakai user, make sure you store this into the {@link #kctl} threadlocal if
   * you are generating it using this method
   */
  private KalturaClient makeKalturaClient() {
    // defaults
    String userKey = "anonymous";
    KalturaSessionType sessionType = KalturaSessionType.USER;
    // NOTE: there is no way to get the user outside of a request in OAE
    KalturaClient kc = makeKalturaClient(userKey, sessionType, 0);
    return kc;
  }

  /**
   * threadsafe method to get a kaltura client
   * 
   * @param userKey
   *          the user key (normally should be the username)
   * @return the current kaltura client for this thread
   */
  public KalturaClient getKalturaClient(String userKey) {
    if (userKey != null && !"".equals(userKey)) {
      KalturaClient kc = makeKalturaClient(userKey, KalturaSessionType.ADMIN, 0);
      kctl.set(kc);
    }
    return kctl.get();
  }

  /**
   * Get the KME with a permissions check to make sure the user key matches
   * 
   * @param keid
   *          the kaltura entry id
   * @param entryService
   *          the katura entry service
   * @return the entry
   * @throws KalturaApiException
   *           if kaltura cannot be accessed
   * @throws IllegalArgumentException
   *           if the keid cannot be found for this user
   */
  private KalturaBaseEntry getKalturaEntry(String userKey, String keid,
      KalturaBaseEntryService entryService) throws KalturaApiException {
    // DO NOT CACHE THIS ONE
    KalturaBaseEntry entry = null;
    // Cannot use the KMEF because it cannot filter by id correctly -AZ
    /*
     * KalturaBaseEntryFilter kmef = new KalturaBaseEntryFilter();
     * kmef.partnerIdEqual = this.kalturaConfig.getPartnerId(); kmef.userIdEqual
     * = currentUserName; kmef.idEqual = keid; //kmef.orderBy = "title";
     * KalturaMediaListResponse listResponse = mediaService.list(kmef); if
     * (listResponse != null && ! listResponse.objects.isEmpty()) { kme =
     * listResponse.objects.get(0); // just get the first one }
     */
    // have to use - mediaService.get(keid); despite it not even checking if we
    // have access to this - AZ
    entry = entryService.get(keid);
    if (entry == null) {
      // did not find the item by keid so we die
      throw new IllegalArgumentException("Cannot find kaltura item (" + keid
          + ") with for user (" + userKey + ")");
    }
    // also do a manual check for security, not so sure about this check though
    // -AZ
    if (entry.partnerId != this.kalturaConfig.getPartnerId()) {
      throw new SecurityException("KME partnerId (" + entry.partnerId
          + ") does not match current one ("
          + this.kalturaConfig.getPartnerId() + "), cannot access this KME ("
          + keid + ")");
    }
    return entry;
  }

  /**
   * 
   * @param userId
   * @param fileName
   * @param fileSize
   * @param inputStream
   * @param mediaType
   * @param title
   * @param description
   * @param tags
   * @return
   */
  public KalturaBaseEntry uploadItem(String userId, String fileName,
      long fileSize, InputStream inputStream, KalturaMediaType mediaType,
      String title, String description, String tags) {
    if (title == null || "".equals(title)) {
      title = fileName;
    }
    if (mediaType == null) {
      mediaType = KalturaMediaType.VIDEO;
    }
    KalturaMediaEntry kme = null;
    KalturaClient kc = getKalturaClient(userId); // force this to be an admin
                                                 // key
    if (kc != null) {
      try {
        String uploadTokenId = kc.getMediaService().upload(inputStream,
            fileName, fileSize);
        // LOG.info("upload token result: "+uploadTokenId);
        KalturaMediaEntry mediaEntry = new KalturaMediaEntry();
        mediaEntry.mediaType = KalturaMediaType.VIDEO;
        mediaEntry.userId = userId;
        mediaEntry.name = title;
        if (description != null) {
          mediaEntry.description = description;
        }
        if (tags != null) {
          mediaEntry.tags = tags;
        }
        mediaEntry.adminTags = "OAE"; // Should we handle with custom meta
                                      // fields instead (for 9 July 2011, we
                                      // will not)?
        kme = kc.getMediaService().addFromUploadedFile(mediaEntry,
            uploadTokenId);
        // kme = kc.getBaseEntryService().update(entryId, mediaEntry); // NOTE:
        // updateKalturaItem()
      } catch (Exception e) {
        LOG.error("Failure uploading item (" + fileName + "): " + e, e);
        throw new RuntimeException(e);
      }
    }
    return kme;
  }

  // ------- OAE Content Methods -----

  /**
   * Retrieve an OAE content item
   * 
   * @param poolId
   *          the unique path/poolId of a content object
   * @return the Content object
   * @throws RuntimeException
   *           if the content object cannot be retrieved
   */
  private Content getContent(String poolId) {
    Content content = null;
    try {
      Session adminSession = repository.loginAdministrative();
      ContentManager cm = adminSession.getContentManager();
      content = cm.get(poolId);
      adminSession.logout();
    } catch (Exception e) {
      LOG.error("Unable to get content by path=" + poolId + ": " + e, e);
      throw new RuntimeException("Unable to get content by path=" + poolId
          + ": " + e, e);
    }
    if (content == null) {
      throw new RuntimeException("Unable to get content by path=" + poolId
          + ": item not found");
    }
    return content;
  }

  /**
   * Update an OAE content item
   * 
   * @param poolId
   *          the unique path/poolId of a content object
   * @param properties
   *          the properties to update or delete on this object (props with a
   *          NULL value will be removed, all others will be replaced or added)
   * @return the complete set of new properties for the content
   * @throws RuntimeException
   *           if the content object cannot be updated
   */
  private Map<String, Object> updateContent(String poolId, Map<?, ?> properties) {
    Map<String, Object> props = null;
    Content contentItem = getContent(poolId);
    // dumpMapToLog(properties, "NEW-properties");
    for (Entry<?, ?> entry : properties.entrySet()) {
      String key = (String) entry.getKey();
      Object val = entry.getValue();
      if (val != null) {
        contentItem.setProperty(key, val);
      } else {
        contentItem.removeProperty(key);
      }
    }
    try {
      Session adminSession = repository.loginAdministrative();
      ContentManager contentManager = adminSession.getContentManager();
      contentManager.update(contentItem);
      Content content = contentManager.get(poolId);
      props = content.getProperties();
      adminSession.logout();
      LOG.debug("Completed update of content item props (" + poolId
          + ") for Kaltura upload");
    } catch (Exception e) {
      LOG.error("Unable to update content at path=" + poolId + ": " + e, e);
      throw new RuntimeException("Unable to update content at path=" + poolId
          + ": " + e, e);
    }
    return props;
  }

}
