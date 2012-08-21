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
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.ConnectionFactory;
import javax.jms.Connection;
import javax.jms.MessageListener;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Queue;
import javax.jms.Message;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQSession;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;

import org.sakaiproject.nakamura.api.files.FilesConstants;

import org.sakaiproject.nakamura.api.media.MediaMetadata;
import org.sakaiproject.nakamura.api.media.MediaService;
import org.sakaiproject.nakamura.api.media.MediaStatus;
import org.sakaiproject.nakamura.api.media.MediaServiceException;
import org.sakaiproject.nakamura.api.media.ErrorHandler;
import org.sakaiproject.nakamura.util.telemetry.TelemetryCounter;


public class MediaCoordinator implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(MediaCoordinator.class); 

  protected Repository sparseRepository;

  private ConnectionFactory connectionFactory;
  private String queueName;
  private AtomicBoolean running;

  private Connection conn = null;
  private Session jmsSession = null;
  private MessageConsumer mediaQueueConsumer = null;

  Thread activeThread;

  private MediaService mediaService;
  private MediaTempFileStore mediaTempStore;
  private ErrorHandler errorHandler = null;

  private int maxRetries;
  private int retryMs;
  private int workerCount;
  private int pollFrequency;


  public MediaCoordinator(ConnectionFactory connectionFactory, String queueName,
      Repository sparseRepository, MediaTempFileStore mediaTempStore,
      MediaService mediaService, int maxRetries,
      int retryMs, int workerCount, int pollFrequency) {

    this.connectionFactory = connectionFactory;
    this.queueName = queueName;
    this.sparseRepository = sparseRepository;
    this.mediaTempStore = mediaTempStore;
    this.mediaService = mediaService;

    this.maxRetries = maxRetries;
    this.retryMs = retryMs;
    this.workerCount = Math.max(1, workerCount);
    this.pollFrequency = pollFrequency;

    running = new AtomicBoolean(false);
  }


  public void start() {
    running.set(true);

    activeThread = new Thread(this);
    activeThread.setName("MediaCoordinator thread");
    activeThread.start();
  }


  public void shutdown() {
    running.set(false);

    if (activeThread != null) {
      try {
        activeThread.interrupt();
        activeThread.join();
      } catch (InterruptedException e) {
        LOGGER.error("Caught InterruptedException on shutdown: {}", e);
      }
    } else {
      LOGGER.info("No thread active");
    }
  }


  public boolean isAcceptedMediaType(String mimeType) {
    String extension = MediaUtils.mimeTypeToExtension(mimeType);

    LOGGER.info("Media mime type and extension: {} AND {}", mimeType, extension);

    return (mimeType != null && extension != null &&
        mediaService.acceptsFileType(mimeType, extension));
  }


  /**
   * If poolId looks like a media file, set its mime type to have it handled by the media service
   * @param poolId A content path
   */
  public void maybeMarkAsMedia(String poolId) {
    org.sakaiproject.nakamura.api.lite.Session adminSession = null;

    try {
      adminSession = sparseRepository.loginAdministrative();
      ContentManager cm = adminSession.getContentManager();
      Content obj = cm.get(poolId);

      String mimeType = (String)obj.getProperty(FilesConstants.POOLED_CONTENT_MIMETYPE);

      if (isAcceptedMediaType(mimeType)) {
        String extension = MediaUtils.mimeTypeToExtension(mimeType);

        obj = cm.get(poolId);
        obj.setProperty(FilesConstants.POOLED_CONTENT_MIMETYPE, mediaService.getMimeType());
        obj.setProperty("media:extension", extension);

        cm.update(obj);
      }
    } catch (ClientPoolException e) {
      LOGGER.warn("ClientPoolException when handling file: {}", e);
    } catch (StorageClientException e) {
      LOGGER.warn("StorageClientException when handling file: {}", e);
    } catch (AccessDeniedException e) {
      LOGGER.warn("AccessDeniedException when handling file: {}", e);
    } finally {
      if (adminSession != null) {
        try {
          adminSession.logout();
        } catch (Exception e) {
          LOGGER.warn("Failed to logout of administrative session {} ",
              e.getMessage());
        }
      }
    }
  }


  public void recordTempVersion(String poolId, String tempVersion) {

    org.sakaiproject.nakamura.api.lite.Session adminSession = null;

    try {
      adminSession = sparseRepository.loginAdministrative();
      ContentManager cm = adminSession.getContentManager();
      Content obj = cm.get(poolId);

      obj.setProperty("media:tempStoreLocation", tempVersion);

      cm.update(obj);

    } catch (ClientPoolException e) {
      LOGGER.warn("ClientPoolException when handling file: {}", e);
    } catch (StorageClientException e) {
      LOGGER.warn("StorageClientException when handling file: {}", e);
    } catch (AccessDeniedException e) {
      LOGGER.warn("AccessDeniedException when handling file: {}", e);
    } finally {
      if (adminSession != null) {
        try {
          adminSession.logout();
        } catch (Exception e) {
          LOGGER.warn("Failed to logout of administrative session {} ",
              e.getMessage());
        }
      }
    }
  }


  private void connectToJMS(final LinkedBlockingQueue<Message> incoming) {
    try {
      conn = connectionFactory.createConnection();
      conn.start();

      jmsSession = conn.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE);

      Queue mediaQueue = jmsSession.createQueue(queueName);
      mediaQueueConsumer = jmsSession.createConsumer(mediaQueue);

      mediaQueueConsumer.setMessageListener(new MessageListener() {
          public void onMessage(Message msg) {
            try {
              LOGGER.info("Received JMS message for pid {}", msg.getStringProperty("pid"));
            } catch (JMSException e) {
              LOGGER.warn("JMS exception onMessage: {}", e);
            }
            incoming.add(msg);
          }
        });

    } catch (JMSException e) {
      LOGGER.error("Fatal: Can't connect to JMS queue.  Aborting.");
      throw new RuntimeException(e.getMessage(), e);
    }
  }


  private void disconnectFromJMS() {
    try {
      mediaQueueConsumer.close();
    } catch (JMSException e) {
      LOGGER.error("Problems when disconnecting from JMS: {}", e);
    }

    try {
      jmsSession.close();
    } catch (JMSException e) {
      LOGGER.error("Problems when disconnecting from JMS: {}", e);
    }

    try {
      conn.close();
    } catch (JMSException e) {
      LOGGER.error("Problems when disconnecting from JMS: {}", e);
    }
  }


  private ExecutorService[] createWorkerPool() {
    ExecutorService[] pool = new ExecutorService[workerCount];

    for (int i = 0; i < workerCount; i++) {
      pool[i] = Executors.newFixedThreadPool(1);
    }

    return pool;
  }


  private boolean isMedia(String mimeType) {
    return mediaService.acceptsFileType(mimeType, null);
  }


  private void syncMedia(String path, Content obj, ContentManager cm) throws IOException {
    LOGGER.info("Processing media now ({})...", obj);

    try {
      MediaNode mediaNode = MediaNode.get(path, cm, true);

      // if the content object is null, treat this as a delete
      if (obj == null) {
        LOGGER.debug("Deleting media content for " + path);

        List<String> mediaIds = mediaNode.getMediaIds();
        for (String mediaId : mediaIds) {
          try {
            LOGGER.debug("Deleting media [{}]", mediaId);
            TelemetryCounter.incrementValue("media", "Coordinator", "deletes-started");

            mediaService.deleteMedia(mediaId);

            LOGGER.debug("Deleted media [{}]", mediaId);
            TelemetryCounter.incrementValue("media", "Coordinator", "deletes-finished");
          } catch (MediaServiceException e) {
            LOGGER.error("Got MediaServiceException during delete [" + mediaId + "]", e);
          }
        }
      } else {
        VersionManager vm = new VersionManager(cm);

        for (Version version : vm.getVersionsMetadata(path)) {
          LOGGER.info("Processing version {} of object {}",
              version, path);

          LOGGER.info("Version particulars: {} and {}", version.getMimeType(), version.getExtension());

          if (!isMedia(version.getMimeType())) {
            LOGGER.info("This version isn't a video.  Skipped.");
            TelemetryCounter.incrementValue("media", "Coordinator", "skips");
            continue;
          }

          if (!mediaNode.isBodyUploaded(version)) {
            LOGGER.info("Uploading body for version {} of object {}",
                version, path);

            File mediaFile = mediaTempStore.getFile(path, version.getTempStoreLocation());

            if (mediaFile == null) {
              LOGGER.error("Couldn't find temp file for path {}, location {}",
                  path, version.getTempStoreLocation());
              System.out.println("Missing " + path + " " + version.getTempStoreLocation());
              return;
            }

            try {
              TelemetryCounter.incrementValue("media", "Coordinator", "uploads-started");
              MediaMetadata metadata = new MediaMetadata(version.getTitle(),
                  version.getDescription(),
                  version.getTags(),
                  version.getExtension(),
                  version.getVersionId(),
                  String.valueOf(obj.getProperty(Content.CREATED_BY_FIELD)));
              String mediaId = mediaService.createMedia(mediaFile, metadata);
              TelemetryCounter.incrementValue("media", "Coordinator", "uploads-finished");

              mediaNode.storeMediaId(version, mediaId);

              mediaTempStore.completed(path, version.getTempStoreLocation());

            } catch (MediaServiceException e) {
              throw new RuntimeException("Got MediaServiceException during body upload", e);
            }
          }

          if (!mediaNode.isMetadataUpToDate(version)) {
            LOGGER.info("Updating metadata for version {} of object {}",
                version, path);

            try {
              TelemetryCounter.incrementValue("media", "Coordinator", "updates-started");
              MediaMetadata metadata = new MediaMetadata(mediaNode.getMediaId(version),
                  version.getTitle(),
                  version.getDescription(),
                  version.getTags(),
                  String.valueOf(obj.getProperty(Content.CREATED_BY_FIELD)));
              mediaService.updateMedia(metadata);
              TelemetryCounter.incrementValue("media", "Coordinator", "updates-finished");

              mediaNode.recordVersion(version);

            } catch (MediaServiceException e) {
              throw new RuntimeException("Got MediaServiceException during metadata update", e);
            }
          }

          if (mediaNode.isBodyUploaded(version) && !mediaNode.isReadyToPlay(version)) {
            // Check if the media has been made available for playing since we last looked
            LOGGER.info("Checking whether version {} is ready to play", version);

            String mediaId = mediaNode.getMediaId(version);
            try {
              MediaStatus status = mediaService.getStatus(mediaId);

              if (status.isReady()) {
                mediaNode.setReadyToPlay(version);
              }
            } catch (MediaServiceException e) {
              throw new RuntimeException("Got MediaServiceException during metadata update", e);
            }
          }
        }
      }

    } catch (StorageClientException e) {
      LOGGER.warn("StorageClientException when syncing media: {}",
          path);
    } catch (AccessDeniedException e) {
      LOGGER.warn("AccessDeniedException when syncing media: {}",
          path);
    }
  }


  private void processObject(String pid) throws IOException {
    org.sakaiproject.nakamura.api.lite.Session sparseSession = null;
    try {
      sparseSession = sparseRepository.loginAdministrative();

      ContentManager contentManager = sparseSession.getContentManager();
      Content obj = contentManager.get(pid);

      if (obj != null) {
        String mimeType = (String)obj.getProperty(FilesConstants.POOLED_CONTENT_MIMETYPE);

        if (!isMedia(mimeType)) {
          LOGGER.info("Path '{}' isn't a media file (type is: {}).  Skipped.",
              pid, mimeType);
          TelemetryCounter.incrementValue("media", "Coordinator", "skips");
          return;
        }
      }

      syncMedia(pid, obj, contentManager);

    } catch (StorageClientException e) {
      LOGGER.warn("StorageClientException while processing {}: {}",
          pid, e);
    } catch (AccessDeniedException e) {
      LOGGER.warn("AccessDeniedException while processing {}: {}",
          pid, e);
    } finally {
      try {
        if (sparseSession != null) {
          sparseSession.logout();
        }
      } catch (Exception e) {
        LOGGER.warn("Failed to logout of administrative session {} ",
            e.getMessage());
      }
    }
  }


  private void clearDuplicates(LinkedBlockingQueue<Message> incoming, String pid) {
    try {
      // A bit funny to wait like this, but if we give a new file upload a
      // few seconds we can usually handle the initial upload and all its
      // events in one shot.  Just a dumb optimisation.
      Thread.sleep(pollFrequency);
    } catch (InterruptedException e) {}

    for (Message msg : incoming) {
      try {
        if (pid.equals(msg.getStringProperty("pid")) &&
            incoming.remove(msg)) {

          // We're about to handle this message, so skip the others
          LOGGER.info("Discarded duplicate message: {}", msg);
          msg.acknowledge();
        }
      } catch (JMSException e) {
        LOGGER.warn("Got a JMSException while clearing duplicates: {}",
            e);
      }
    }
  }


  private class FailedJob {
    public String jobId;
    public long time;

    public FailedJob(String jobId, long time) {
      this.jobId = jobId;
      this.time = time;
    }
  }


  public void setErrorHandler(ErrorHandler handler) {
    this.errorHandler = handler;
  }


  public void run() {
    LOGGER.info("Running MediaCoordinator");

    final LinkedBlockingQueue<Message> incoming = new LinkedBlockingQueue<Message>();
    final LinkedBlockingQueue<String> completed = new LinkedBlockingQueue<String>();
    final LinkedBlockingQueue<FailedJob> failed = new LinkedBlockingQueue<FailedJob>();

    connectToJMS(incoming);

    ExecutorService[] workers = createWorkerPool();
    Map<String, Message> inProgress = new HashMap<String, Message>();
    Map<String, Integer> retryCounts = new HashMap<String, Integer>();

    try {
      Slowdown slowdown = new Slowdown((long)(pollFrequency));
      while (running.get()) {
        try {
          Message msg = null;
          try {
            msg = incoming.poll(pollFrequency, TimeUnit.MILLISECONDS);
          } catch (InterruptedException e) {}

          if (!running.get()) {
            break;
          }

          if (msg != null) {
            final String jobId = msg.getJMSMessageID();
            final String pid = msg.getStringProperty("pid");

            if (pid == null) {
              LOGGER.error("Discarded dud message: {}", msg);
              msg.acknowledge();
              continue;
            }

            LOGGER.info("Pulled pid from queue: {}", pid);

            clearDuplicates(incoming, pid);

            inProgress.put(jobId, msg);

            // The hashing here ensures that same PID must always runs
            // on the same worker.  Workers will need to update the
            // content object being synced, so this is our concurrency
            // control.
            ExecutorService worker = workers[Math.abs(pid.hashCode() % workerCount)];

            LOGGER.info("Running pid '{}' on worker: {}", pid,
                Math.abs(pid.hashCode() % workerCount));

            worker.execute(new Runnable() {
                public void run() {
                  LOGGER.info("Worker processing " + pid);

                  try {
                    processObject(pid);

                    completed.add(jobId);
                    LOGGER.info("Worker completed processing {}",
                        pid);

                  } catch (Exception e) {
                    LOGGER.warn("Failed while processing PID '{}'", pid);

                    LOGGER.warn("This job will be queued for retry in {} ms",
                        retryMs);

                    failed.add(new FailedJob(jobId, System.currentTimeMillis()));
                  }
                }
              });

            LOGGER.info("Media waiting to process: {}; " +
                "Media in progress: {}",
                incoming.size(), inProgress.size());
          }

          // Remove objects marked as completed from the "in progress"
          // queue
          while (!completed.isEmpty()) {
            String jobId = completed.poll();

            Message completedMsg = inProgress.get(jobId);

            if (completedMsg != null) {
              LOGGER.info("Completed processing: {} (pid: {})",
                  jobId,
                  completedMsg.getStringProperty("pid"));

              completedMsg.acknowledge();
              inProgress.remove(jobId);
            }
          }


          // Requeue jobs in the failed queue if their retry time has elapsed
          if (!failed.isEmpty()) {
            try {
              while (true) {
                FailedJob job = failed.peek();

                if (job != null && (System.currentTimeMillis() - job.time) > retryMs) {
                  job = failed.take();
                  Message failedMsg = inProgress.get(job.jobId);

                  if (failedMsg != null) {
                    String pid = failedMsg.getStringProperty("pid");

                    inProgress.remove(job.jobId);
                    int retriesSoFar = retryCounts.containsKey(pid) ? retryCounts.get(pid) : 0;

                    if (maxRetries >= 0 && (retriesSoFar + 1) > maxRetries) {
                      LOGGER.error("Giving up on {} after {} failed retry attempts.",
                          pid, retriesSoFar);
                      TelemetryCounter.incrementValue("media", "Coordinator", "failures");

                      retryCounts.remove(pid);
                      failedMsg.acknowledge();

                      if (errorHandler != null) {
                        errorHandler.error(pid);
                      }
                    } else {
                      int retry = retriesSoFar + 1;
                      LOGGER.info("Requeueing job for pid '{}' (retry #{})", pid, retry);
                      TelemetryCounter.incrementValue("media", "Coordinator", "retries");

                      retryCounts.put(pid, retry);
                      incoming.add(failedMsg);
                    }
                    
                  }
                } else {
                  break;
                }
              }
            } catch (InterruptedException ex) {
            }
          }


        } catch (JMSException e) {
          LOGGER.error("JMS exception while waiting for message: {}", e);
          LOGGER.error("Waiting {} ms before trying again",
              pollFrequency);
        } catch (Exception e) {
          LOGGER.error("Got exception in MediaCoordinator main loop: {}", e);
        }

        // Paranoia...
        slowdown.sleep();
      }

      LOGGER.info("Shutting down worker pool...");
      for (ExecutorService worker : workers) {
        worker.shutdownNow();
      }
    } finally {
      LOGGER.info("Disconnecting from the message queue");
      disconnectFromJMS();
    }
  }
}
