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
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.api.media.MediaService;
import org.sakaiproject.nakamura.api.media.MediaServiceException;
import org.sakaiproject.nakamura.api.media.MediaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
@Component(metatype = true)
@Service
@Property(name = "alias", value = "/var/media")
public class MediaServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(MediaServlet.class);
  private String VERSION_ID = "_previousVersion";

  @Reference
  private Repository repository;

  @Reference
  protected EventAdmin eventAdmin;

  @Reference
  MediaService mediaService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    try {
      String pid = req.getParameter("pid");

      if (pid == null) {
        throw new ServletException("Need a value for parameter 'pid'");
      }

      Session adminSession = repository.loginAdministrative();
      ContentManager cm = adminSession.getContentManager();

      VersionManager vm = new VersionManager(cm);

      JSONObject responseJSON = new JSONObject();

      MediaNode mediaNode = MediaNode.get(pid, cm, false);

      Version version = vm.getLatestVersionOf(pid);
      String mediaId = (mediaNode == null) ? null : mediaNode.getMediaId(version);

      if (mediaNode == null || mediaId == null) {
        // The media coordinator hasn't picked up this job yet.
        responseJSON.put("status", "processing");
      } else {

        boolean isReadyToPlay = mediaNode.isReadyToPlay(version);
        MediaStatus status = null;

        if (!isReadyToPlay) {
          status = mediaService.getStatus(mediaId);
        }

        if (isReadyToPlay || status.isReady()) {

          if (!isReadyToPlay) {
            // The media node hasn't recorded the fact that this video is now
            // playable.  Fire an event to prompt it to update to avoid needing
            // to hit the media service the next time someone asks for this
            // video.

            Dictionary<String, Object> props = new Hashtable<String, Object>();
            props.put("path", pid);
            props.put("op", "update");
            props.put("resourceType", "sakai/pooled-content");

            eventAdmin.postEvent(new Event("org/sakaiproject/nakamura/media/UPDATED", props));
          }

          responseJSON.put("status", "ready");
          responseJSON.put("scripts", Arrays.asList(mediaService.getPlayerJSUrls(mediaId)));
          responseJSON.put("player", mediaService.getPlayerFragment(mediaId));
          responseJSON.put("init", mediaService.getPlayerInitJS(mediaId));
        } else if (status.isProcessing()) {
          responseJSON.put("status", "processing");
        } else if (status.isError()) {
          responseJSON.put("status", "error");
        } else {
          responseJSON.put("status", "unknown");
        }
      }
      resp.setContentType("application/json");
      responseJSON.write(resp.getWriter());

    } catch (MediaServiceException e) {
      throw new ServletException(e.getMessage(), e);
    } catch (JSONException e) {
      throw new ServletException(e.getMessage(), e);
    } catch (ClientPoolException e) {
      throw new ServletException(e.getMessage(), e);
    } catch (StorageClientException e) {
      throw new ServletException(e.getMessage(), e);
    } catch (AccessDeniedException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    super.doDelete(req, resp);
  }
}
