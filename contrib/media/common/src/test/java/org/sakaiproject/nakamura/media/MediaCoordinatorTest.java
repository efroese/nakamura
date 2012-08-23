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

import java.io.IOException;
import java.io.File;
import java.io.ByteArrayInputStream;

import java.lang.reflect.Field;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.sakaiproject.nakamura.api.media.MediaMetadata;
import org.sakaiproject.nakamura.api.media.MediaService;
import org.sakaiproject.nakamura.api.media.ErrorHandler;
import org.sakaiproject.nakamura.api.media.MediaStatus;
import org.sakaiproject.nakamura.api.media.MediaServiceException;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.lite.BaseMemoryRepository;
import org.sakaiproject.nakamura.media.MediaCoordinator;
import org.sakaiproject.nakamura.media.MediaUtils;

import org.sakaiproject.nakamura.api.lite.Session;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import com.google.common.collect.ImmutableMap;


public class MediaCoordinatorTest {

  private static String TEST_QUEUE = "MEDIA_TEST";
  private static String JMS_URL = "tcp://localhost:12331";
  private static BrokerService broker;
  private static ActiveMQConnectionFactory connectionFactory;

  private int POLL_MS = 100;
  private int MAX_WAIT_MS = 10000;

  private class MockMediaService implements MediaService {

    public List<Map<String, String>> created = Collections.synchronizedList(new ArrayList<Map<String, String>>());
    public List<Map<String, String>> updated = Collections.synchronizedList(new ArrayList<Map<String, String>>());

    public boolean alwaysFail = false;
    public boolean failOnNextCreate = false;

    volatile public int failCount = 0;

    public Map<String, Object> createMedia(File media, MediaMetadata metadata)
        throws MediaServiceException {

      if (failOnNextCreate) {
        failOnNextCreate = false;
        failCount++;
        throw new RuntimeException("Transient failure!");
      }

      if (alwaysFail) {
        failCount++;
        throw new RuntimeException("Permanent failure!");
      }


      created.add(ImmutableMap.of("title", metadata.getTitle(),
                                  "description", metadata.getDescription(),
                                  "extension", metadata.getExtension(),
                                  "tags", Arrays.asList(metadata.getTags()).toString()));

      Map<String, Object> meta = new HashMap<String, Object>(1);
      meta.put("bodyMediaId", String.valueOf(System.currentTimeMillis()));
      return meta;
    }


    public String updateMedia(MediaMetadata metadata)
        throws MediaServiceException {

      updated.add(ImmutableMap.of("id", metadata.getId(),
                                  "title", metadata.getTitle(),
                                  "description", metadata.getDescription(),
                                  "tags", Arrays.asList(metadata.getTags()).toString()));

      return metadata.getId();
    }

    public MediaStatus getStatus(String id) throws MediaServiceException, IOException {
      return new MediaStatus() {
        public boolean isReady() {
          return true;
        }

        public boolean isProcessing() {
          return false;
        }

        public boolean isError() {
          return false;
        }
      };
    }

    public String getPlayerFragment(String id) {
      return "[the player]";
    }

    public String[] getPlayerJSUrls(String id) {
      return new String[] {};
    }

    public String getPlayerInitJS(String id) {
      return "";
    }

    public String getMimeType() {
      return "application/x-media-testsuite";
    }

    public boolean acceptsFileType(String mimeType, String extension) {
      // Sure!
      return "application/x-media-testsuite".equals(mimeType);
    }


    @Override
    public void deleteMedia(String id) throws MediaServiceException {
    }
  }


  private Repository repository;
  private Session adminSession;
  private ContentManager cm;
  private MediaTempFileStore mediaTempStore;

  MockMediaService mediaService;
  MediaCoordinator mc;


  @BeforeClass
  public static void init() throws Exception {
    broker = new BrokerService();
    broker.setBrokerName(TEST_QUEUE);
    broker.addConnector(JMS_URL);

    broker.start();


    connectionFactory = new ActiveMQConnectionFactory(JMS_URL);
  }
  

  @Before
  public void setUp() throws Exception {
    repository = new BaseMemoryRepository().getRepository();
    adminSession = repository.loginAdministrative();
    cm = adminSession.getContentManager();

    mediaTempStore = new MediaTempFileStore(System.getProperty("java.io.tmpdir"));

    mediaService = new MockMediaService();
    mc = new MediaCoordinator(connectionFactory, TEST_QUEUE, repository,
                              mediaTempStore,
                              mediaService,
                              MediaListenerImpl.MAX_RETRIES_DEFAULT, MediaListenerImpl.RETRY_MS_DEFAULT,
                              MediaListenerImpl.WORKER_COUNT_DEFAULT, 500);

    mc.start();
  }


  private void contentUpdated(String pid) {
    try {
      Connection conn = connectionFactory.createConnection();
      javax.jms.Session jmsSession = conn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
      Queue mediaQueue = jmsSession.createQueue(TEST_QUEUE);

      MessageProducer producer = jmsSession.createProducer(mediaQueue);

      producer.send(MediaUtils.message(jmsSession, "pid", pid));

      producer.close();
      jmsSession.close();
      conn.close();

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }


  private void saveVersion(String path, String mimeType, String extension, String title,
                               String description, String ... tags)
    throws Exception {

    String tempId = mediaTempStore.store(new ByteArrayInputStream("hello".getBytes()), path);

    Map<String, Object> props = new HashMap<String, Object>();

    props.put("_path", path);
    props.put("_mimeType", mimeType);
    props.put("media:extension", extension);
    props.put("media:tempStoreLocation", tempId);
    props.put("sakai:pooled-content-file-name", title);
    props.put("sakai:description", description);

    cm.update(new Content(path, props));
    cm.saveVersion(path);
  }


  @Test
  public void testSimpleVideo() throws Exception {

    saveVersion("abc", "application/x-media-testsuite", "mpg", "test video 1", "hello, world");

    contentUpdated("abc");

    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      List<Map<String,String>> created = mediaService.created;

      if (created.size() > 0) {
        assertEquals(created.get(0).get("title"), "test video 1");
        assertEquals(created.get(0).get("description"), "hello, world");

        return;
      }

      Thread.sleep(POLL_MS);
    }

    fail("Video upload didn't arrive in time.");
  }


  @Test
  public void testTwoVersions() throws Exception {

    saveVersion("abc", "application/x-media-testsuite", "mpg", "test video 1", "hello, world");
    saveVersion("abc", "application/x-media-testsuite", "avi", "test video 1", "hello, world (updated)");

    contentUpdated("abc");

    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      List<Map<String,String>> created = mediaService.created;

      if (created.size() == 2) {
        assertTrue(("mpg".equals(created.get(0).get("extension")) && "avi".equals(created.get(1).get("extension"))) ||
                   ("avi".equals(created.get(0).get("extension")) && "mpg".equals(created.get(1).get("extension"))));

        return;
      }

      Thread.sleep(POLL_MS);
    }

    fail("Video upload didn't arrive in time.");
  }


  @Test
  public void testNonVideoVersion() throws Exception {

    saveVersion("abc", "application/x-media-testsuite", "mpg", "test video 1", "hello, world");
    saveVersion("abc", "image/png", "png", "test video 1", "this isn't a video");
    saveVersion("abc", "application/x-media-testsuite", "avi", "test video 1", "hello, world (updated)");

    contentUpdated("abc");

    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      List<Map<String,String>> created = mediaService.created;

      if (created.size() == 2) {
        assertTrue(("mpg".equals(created.get(0).get("extension")) && "avi".equals(created.get(1).get("extension"))) ||
                   ("avi".equals(created.get(0).get("extension")) && "mpg".equals(created.get(1).get("extension"))));

        return;
      }

      Thread.sleep(POLL_MS);
    }

    fail("Video upload didn't arrive in time.");
  }


  @Test
  public void testNonVideoInitialVersion() throws Exception {

    saveVersion("abc", "image/png", "png", "test video 1", "this isn't a video");
    saveVersion("abc", "application/x-media-testsuite", "mpg", "test video 1", "hello, world");
    saveVersion("abc", "application/x-media-testsuite", "avi", "test video 1", "hello, world (updated)");

    contentUpdated("abc");

    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      List<Map<String,String>> created = mediaService.created;

      if (created.size() == 2) {
        assertTrue(("mpg".equals(created.get(0).get("extension")) && "avi".equals(created.get(1).get("extension"))) ||
                   ("avi".equals(created.get(0).get("extension")) && "mpg".equals(created.get(1).get("extension"))));

        return;
      }

      Thread.sleep(POLL_MS);
    }

    fail("Video upload didn't arrive in time.");
  }


  @Test
  public void testUpdateMetadata() throws Exception {

    saveVersion("abc", "application/x-media-testsuite", "mpg", "test video 1", "hello, world");

    contentUpdated("abc");

    boolean wasCreated = false;
    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      List<Map<String,String>> created = mediaService.created;

      if (created.size() == 1) {
        assertEquals(created.get(0).get("title"), "test video 1");

        wasCreated = true;
        break;
      }

      Thread.sleep(POLL_MS);
    }

    if (!wasCreated) {
      fail("Video upload didn't arrive in time.");
      return;
    }

    // Update the metadata without creating a new version and verify that it propagates
    Content obj = cm.get("abc");
    obj.setProperty("sakai:description", "a new description");
    obj.setProperty("sakai:tags", new String[] { "anewtag", "anothernewtag" });
    cm.update(obj);

    contentUpdated("abc");

    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      List<Map<String,String>> updated = mediaService.updated;

      if (updated.size() == 1) {
        assertEquals(updated.get(0).get("description"), "a new description");
        assertEquals(updated.get(0).get("tags"), "[anewtag, anothernewtag]");

        return;
      }

      Thread.sleep(POLL_MS);
    }

    fail("Video metadata update didn't arrive in time.");
  }


  @Test
  public void testRetryOnFail() throws Exception {

    mediaService.failOnNextCreate = true;

    Field retry_ms = MediaCoordinator.class.getDeclaredField("retryMs");
    retry_ms.setAccessible(true);
    retry_ms.setInt(mc, 200);

    saveVersion("abc", "application/x-media-testsuite", "mpg", "test video 1", "hello, world");
    contentUpdated("abc");

    boolean wasCreated = false;
    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      List<Map<String,String>> created = mediaService.created;

      if (created.size() == 1) {
        // The first attempt failed...
        assertFalse(mediaService.failOnNextCreate);

        // ... but we got there in the end.
        assertEquals(created.get(0).get("title"), "test video 1");

        return;
      }

      Thread.sleep(POLL_MS);
    }

    if (!wasCreated) {
      fail("Video upload didn't arrive in time.");
      return;
    }
  }


  @Test
  public void testMaxRetries() throws Exception {

    mediaService.alwaysFail = true;

    Field retry_ms = MediaCoordinator.class.getDeclaredField("retryMs");
    retry_ms.setAccessible(true);
    retry_ms.setInt(mc, 200);

    Field max_retries = MediaCoordinator.class.getDeclaredField("maxRetries");
    max_retries.setAccessible(true);
    max_retries.setInt(mc, 2);

    final Semaphore semaphore = new Semaphore(0);

    mc.setErrorHandler(new ErrorHandler() {
        public void error(String pid) {
          if ("abc".equals(pid)) {
            semaphore.release();
          }
        }
      });

    saveVersion("abc", "application/x-media-testsuite", "mpg", "test video 1", "hello, world");
    contentUpdated("abc");

    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      if (semaphore.tryAcquire(POLL_MS, TimeUnit.MILLISECONDS)) {
        return;
      }
    }

    fail("Failed upload didn't give up in time");
  }


  @Test
  public void testMalformedMessage() throws Exception {

    contentUpdated(null);

    saveVersion("abc", "application/x-media-testsuite", "mpg", "test video 1", "hello, world");

    contentUpdated("abc");

    for (int i = 0; i < (MAX_WAIT_MS / POLL_MS); i++) {
      List<Map<String,String>> created = mediaService.created;

      if (created.size() > 0) {
        assertEquals(created.get(0).get("title"), "test video 1");
        assertEquals(created.get(0).get("description"), "hello, world");

        return;
      }

      Thread.sleep(POLL_MS);
    }

    fail("Malformed message killed the queue.");


  }

  @After
  public void tearDown() throws Exception {
    mc.shutdown();
    adminSession.logout();
  }


  @AfterClass
  public static void destroy() throws Exception {
    broker.stop();
  }
}
