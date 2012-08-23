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
package org.sakaiproject.nakamura.media.matterhorn;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthChallengeParser;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.DigestScheme;
import org.apache.commons.httpclient.auth.MalformedChallengeException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentException;
import org.sakaiproject.nakamura.api.media.MediaMetadata;
import org.sakaiproject.nakamura.api.media.MediaService;
import org.sakaiproject.nakamura.api.media.MediaServiceException;
import org.sakaiproject.nakamura.api.media.MediaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *
 */
@Component(enabled = true, metatype = true, policy = ConfigurationPolicy.REQUIRE)
@Service
public class MatterhornMediaService implements MediaService {

  private static final Logger LOG = LoggerFactory.getLogger(MatterhornMediaService.class);

  public static final String X_MEDIA_MATTERHORN = "application/x-media-matterhorn";

  static final String BASE_URL_DEFAULT = "http://localhost:7080";
  @Property(value = BASE_URL_DEFAULT)
  public static final String BASE_URL = "baseUrl";

  private static final String REQ_MSG_TMPL = "'%s' required to communicate with Matterhorn";

  static final String MATTERHORN_USER_DEFAULT = "matterhorn_system_account";
  @Property(value = MATTERHORN_USER_DEFAULT)
  public static final String MATTERHORN_USER = "matterhornUser";

  static final String MATTERHORN_PASSWORD_DEFAULT = "CHANGE_ME";
  @Property(value = MATTERHORN_PASSWORD_DEFAULT)
  public static final String MATTERHORN_PASSWORD = "matterhornPassword";


  static final String WIDTH_DEFAULT = "500";
  @Property(value = WIDTH_DEFAULT)
  public static final String WIDTH = "width";

  static final String HEIGHT_DEFAULT = "470";
  @Property(value = HEIGHT_DEFAULT)
  public static final String HEIGHT = "height";

  private static final String PLAYER_TMPL = "<iframe src=\"%s/engage/ui/embed.html?id=%s\" " +
    "style=\"border:0px #FFFFFF none;\" name=\"Opencast Matterhorn - Media Player\" " +
    "scrolling=\"no\" frameborder=\"0\" marginheight=\"0px\" marginwidth=\"0px\" " +
    "width=\"%s\" height=\"%s\"></iframe>";



  String height;
  String width;
  String matterhornUser;
  String matterhornPassword;
  String baseUrl;
   

  @Activate
  @Modified
  protected void activate(Map<?, ?> props) {
    // ---------- required properties ------------------------------------------
    Map<String, String> requiredProperties = new HashMap<String, String>();
    for (String prop : new String[] { HEIGHT, WIDTH, MATTERHORN_USER, MATTERHORN_PASSWORD, BASE_URL }) {
      String value = PropertiesUtil.toString(props.get(prop), null);
      if (StringUtils.isBlank(value)) {
        throw new ComponentException(String.format(REQ_MSG_TMPL, prop));
      }

      requiredProperties.put(prop, value);
    }

    height = requiredProperties.get(HEIGHT);
    width = requiredProperties.get(WIDTH);
    matterhornUser = requiredProperties.get(MATTERHORN_USER);
    matterhornPassword = requiredProperties.get(MATTERHORN_PASSWORD);
    baseUrl = requiredProperties.get(BASE_URL);
  }



  private void matterhornAuthenticate(HttpClient client) {

    List authPrefs = new ArrayList(1);
    authPrefs.add(AuthPolicy.DIGEST);
    client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);

    AuthScope scope = new AuthScope(AuthScope.ANY_HOST,
        AuthScope.ANY_PORT);

    UsernamePasswordCredentials credentials =
      new UsernamePasswordCredentials(matterhornUser, matterhornPassword);

    client.getState().setCredentials(scope, credentials);
  }
    

  private Document parseXML(String s) throws MediaServiceException {
    try {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setAttribute("http://xml.org/sax/features/namespaces", Boolean.TRUE);

    return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
    } catch (ParserConfigurationException e) {
      throw new MediaServiceException("Failed to parse XML: " + e);
    } catch (SAXException e) {
      throw new MediaServiceException("Failed to parse XML: " + e);
    } catch (IOException e) {
      throw new MediaServiceException("Failed to parse XML: " + e);
    }
  }


  private String docToString(Node doc) throws MediaServiceException {
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      StringWriter sw = new StringWriter();

      t.transform(new DOMSource(doc), new StreamResult(sw));

      return sw.toString();
    } catch (TransformerException e) {
      throw new MediaServiceException("Failed to convert XML: " +e);
    }
  }


  private String extractMediaId(String response) throws MediaServiceException {
    try {
    XPath xpath = XPathFactory.newInstance().newXPath();

    Document doc = parseXML(response);

    String workflowId = ((Element)xpath.evaluate("//*[local-name() = 'workflow']", doc, XPathConstants.NODE)).getAttribute("id");
    String mediaPackageId = ((Element)xpath.evaluate("//mediapackage", doc, XPathConstants.NODE)).getAttribute("id");
    String trackId = ((Element)xpath.evaluate("//media/track", doc, XPathConstants.NODE)).getAttribute("id");
    String metadataId = ((Element)xpath.evaluate("//metadata/catalog", doc, XPathConstants.NODE)).getAttribute("id");
    
    JSONObject json = new JSONObject()
      .put("workflowId", workflowId)
      .put("mediaPackageId", mediaPackageId)
      .put("trackId", trackId)
      .put("metadataId", metadataId);

    return json.toString();

    } catch (JSONException e) {
      throw new MediaServiceException("Failed to extract media ID from response: " + e);
    } catch (XPathExpressionException e) {
      throw new MediaServiceException("Failed to extract media ID from response: " + e);
    }
  }


  // We send the auth request in advance of the real POST to avoid having to
  // POST our (potentially large) video file twice.  Basically, get the auth
  // challenge by sending an empty request and then only send our real payload
  // when we know auth will succeed.
  private String preAuthenticateRequest(HttpClient client, HttpMethod authRequest)
    throws IOException, MalformedChallengeException, AuthenticationException{
    authRequest.addRequestHeader(new Header("X-Requested-Auth", "Digest"));
    authRequest.setDoAuthentication(false);

    if (client.executeMethod(authRequest) == 401) {
      DigestScheme scheme = new DigestScheme();
      Map challenges = AuthChallengeParser.parseChallenges(authRequest.getResponseHeaders("WWW-Authenticate"));
      String challenge = (String)challenges.values().toArray()[0];

      scheme.processChallenge(challenge);

      UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(matterhornUser, matterhornPassword);
      return scheme.authenticate(credentials, authRequest);
    }

    return null;
  }

  private <M extends HttpMethod> M digestAuthed(HttpClient client, String uri,
      Class<M> method) throws IOException,
      MalformedChallengeException, AuthenticationException {
    Constructor<M> constructor;
    try {
      constructor = method.getConstructor(String.class);
      M httpMethod = constructor.newInstance(baseUrl + uri);

      String auth = preAuthenticateRequest(client, httpMethod);

      if (auth != null) {
        M request = constructor.newInstance(baseUrl + uri);

        request.addRequestHeader(new Header("Authorization", auth));
        request.setDoAuthentication(false);

        return request;
      }
      return null;
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (SecurityException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public String createMedia(final File mediaFile, final MediaMetadata metadata)
      throws MediaServiceException {

    LOG.info("Processing media for {}", metadata.getTitle());

    PostMethod post = null;

    try {
      HttpClient client = new HttpClient();

      post = digestAuthed(client, "/ingest/addMediaPackage", PostMethod.class);

      Part[] parts = new Part[] {
        new StringPart("flavor", "presenter/source"),
        new StringPart("contributor", "Sakai OAE"),
        new StringPart("title", metadata.getTitle()),
        new StringPart("description", metadata.getDescription()),
        new StringPart("subject", StringUtils.join(metadata.getTags(), ", ")),
        new StringPart("creator", ""),
        new FilePart("track",
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

      post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));


      int returnCode = client.executeMethod(post);
      String response = post.getResponseBodyAsString();

      if (returnCode != 200) {
        throw new MediaServiceException("Failure when processing media:" + response);
      }

      return extractMediaId(response);

    } catch (IOException e) {
      LOG.error("IOException while processing media: {}", e);

      throw new MediaServiceException("IOException while processing media:" + e);
    } finally {
      if (post != null) {
        post.releaseConnection();
      }
    }
  }


  private void updateMetadata(JSONObject ids, HttpClient client,
      String title, String description, String[] tags)
    throws IOException, XPathExpressionException, JSONException, MediaServiceException {

    PostMethod post = null;
    GetMethod get = null;
    try {
      // Get the current metadata
      String dcUrl = String.format("/files/mediapackage/%s/%s/dublincore.xml",
          ids.getString("mediaPackageId"),
          ids.getString("metadataId"));

      get = digestAuthed(client, dcUrl, GetMethod.class);

      int returnCode = client.executeMethod(get);
      Document metadata = parseXML(get.getResponseBodyAsString());

      XPath xpath = XPathFactory.newInstance().newXPath();

      Element titleElt = (Element)xpath.evaluate("//*[local-name() = 'title']", metadata, XPathConstants.NODE);
      Element descriptionElt = (Element)xpath.evaluate("//*[local-name() = 'description']", metadata, XPathConstants.NODE);
      Element subjectElt = (Element)xpath.evaluate("//*[local-name() = 'subject']", metadata, XPathConstants.NODE);

      titleElt.setTextContent(title);
      descriptionElt.setTextContent(description);
      subjectElt.setTextContent(StringUtils.join(tags, ", "));

      final String xml = docToString(metadata);

      // Send the updated version back
      post = digestAuthed(client, dcUrl, PostMethod.class);

      Part[] parts = new Part[] {
        new FilePart("content",
            new PartSource () {
              public InputStream createInputStream() {
                return new ByteArrayInputStream(xml.getBytes());
              }

              public String getFileName() {
                return "dublincore.xml";
              }

              public long getLength() {
                return xml.length();
              }
            })
      };

      post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

      returnCode = client.executeMethod(post);
      String response = post.getResponseBodyAsString();

      if ((returnCode / 100) != 2) {
        throw new MediaServiceException("Failure when processing media:" + response);
      }

    } finally {
      if (post != null) {
        post.releaseConnection();
      }
      if (get != null) {
        get.releaseConnection();
      }
    }
  }


  // For some reason empty 'tags' fields cause the player's JavaScript to choke.
  // Removing them entirely doesn't fix it, so setting them back to the defaults
  // here.
  private String extractMediaPackage(Document workflow) throws XPathExpressionException, MediaServiceException
  {
    XPath xpath = XPathFactory.newInstance().newXPath();

    NodeList tags = (NodeList)xpath.evaluate("//tags", workflow, XPathConstants.NODESET);

    for (int i = 0; i < tags.getLength(); i++) {
      Node elt = tags.item(i);

      if (elt.getChildNodes().getLength() == 0) {
        for (String tagname : new String[] { "engage", "player" }) {
          Element tag = workflow.createElement("tag");
          tag.setTextContent(tagname);
          elt.appendChild(tag);
        }
      }
    }

    Element mediaPackageElt = (Element)xpath.evaluate("//*[local-name() = 'mediapackage']", workflow, XPathConstants.NODE);

    return docToString(mediaPackageElt);
  }


  private void refreshIndexes(JSONObject ids, HttpClient client)
    throws JSONException, IOException, XPathExpressionException, MediaServiceException {

    PostMethod post = null;
    GetMethod get = null;

    try {
      // Get the workflow
      String workflowUrl = String.format("/workflow/instance/%s.xml",
          ids.getString("workflowId"));

      get = digestAuthed(client, workflowUrl, GetMethod.class);

      int returnCode = client.executeMethod(get);

      // Extract the media package
      Document workflow = parseXML(get.getResponseBodyAsString());
      String mediaPackageXML = extractMediaPackage(workflow);


      for (String toUpdate : new String[] { "episode", "search" }) {
        post = digestAuthed(client, "/" + toUpdate + "/add", PostMethod.class);

        Part[] parts = new Part[] {
          new StringPart("mediapackage", mediaPackageXML)
        };

        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

        returnCode = client.executeMethod(post);
        String response = post.getResponseBodyAsString();

        if ((returnCode / 100) != 2) {
          throw new MediaServiceException("Failure when processing media:" + response);
        }
      }
    } finally {
      if (post != null) {
        post.releaseConnection();
      }
      if (get != null) {
        get.releaseConnection();
      }
    }
  }



  @Override
  public String updateMedia(MediaMetadata metadata) throws MediaServiceException {

    try {
      JSONObject ids = new JSONObject(metadata.getId());
      HttpClient client = new HttpClient();

      // Get the Dublin Core metadata file
      updateMetadata(ids, client, metadata.getTitle(), metadata.getDescription(), metadata.getTags());
      refreshIndexes(ids, client);


      return metadata.getId();

    } catch (IOException e) {
      throw new MediaServiceException("Failed to update media: " + e);
    } catch (XPathExpressionException e) {
      throw new MediaServiceException("Failed to update media: " + e);
    } catch (JSONException e) {
      throw new MediaServiceException("Failed to parse media ID: " + e);
    }
  }


  /**
   * {@inheritDoc}
   *
   * @see org.sakaiproject.nakamura.api.media.MediaService#acceptsMimeType(String)
   */
  @Override
  public boolean acceptsFileType(String mimeType, String extension) {
    return mimeType.startsWith("video/") || X_MEDIA_MATTERHORN.equals(mimeType);
  }


  @Override
  public String getPlayerFragment(String id) {
    try {
      JSONObject ids = new JSONObject(id);

      return String.format(PLAYER_TMPL,
          baseUrl, ids.getString("mediaPackageId"), width, height);
    } catch (JSONException e) {
      LOG.info("Error while getting JSON fragment for id '{}': {}", id, e);
      return "";
    }
  }


  @Override
  public MediaStatus getStatus(String id) throws MediaServiceException, IOException {

    GetMethod get = null;

    try {
      JSONObject ids = new JSONObject(id);

      String workflowUrl = String.format("/workflow/instance/%s.json",
          ids.getString("workflowId"));

      HttpClient client = new HttpClient();
      get = digestAuthed(client, workflowUrl, GetMethod.class);
      int returnCode = client.executeMethod(get);

      String s = null;
      if ((returnCode / 100) == 2) {
        JSONObject status = new JSONObject(get.getResponseBodyAsString());
        s = status.getJSONObject("workflow").getString("state");
      }

      final String state = s;

      return new MediaStatus() {
        public boolean isReady() {
          return "SUCCEEDED".equals(state);
        }

        public boolean isProcessing() {
          return ("RUNNING".equals(state) || "INSTANTIATED".equals(state));
        }

        public boolean isError() {
          return (!isReady() && !isProcessing());
        }
      };
    } catch (JSONException e) {
      throw new MediaServiceException("Failed to parse media ID: " + e);
    } finally {
      if (get != null) {
        get.releaseConnection();
      }
    }
  }

  @Override
  public void deleteMedia(String id) throws MediaServiceException {
    Exception exception = null;
    try {
      JSONObject ids = new JSONObject(id);
      String mediaPkgId = ids.getString("mediaPackageId");
      String metadataId = ids.getString("metadataId");
      String workflowId = ids.getString("workflowId");

      try {
        LOG.debug("Deleting from search");
        deleteSearch(mediaPkgId);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        exception = e;
      }

      try {
        LOG.debug("Stopping workflow");
        stopWorkflow(workflowId);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        exception = e;
      }

      try {
        LOG.debug("Deleting media package");
        deleteMediaPackage(mediaPkgId, metadataId);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        exception = e;
      }

      try {
        LOG.debug("Deleting episode");
        deleteEpisode(mediaPkgId);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
        exception = e;
      }
    } catch (JSONException e) {
      throw new MediaServiceException(e.getMessage(), e);
    }

    if (exception != null) {
      throw new MediaServiceException(exception.getMessage(), exception);
    }
  }

  /**
   * Delete a media package.
   *
   * <p><code>DELETE /files/mediapackage/{mediaPackageID}/{mediaPackageElementID}</code></p>
   *
   * @param mediaPkgId
   * @param mediaPackageElementId
   * @throws HttpException
   * @throws IOException
   * @throws MediaServiceException
   */
  private void deleteMediaPackage(String mediaPkgId, String mediaPackageElementId)
      throws HttpException, IOException, MediaServiceException {
    String dcUrl = String.format("/files/mediapackage/%s/%s", mediaPkgId,
        mediaPackageElementId);

    HttpClient client = new HttpClient();
    DeleteMethod delete = digestAuthed(client, dcUrl, DeleteMethod.class);

    // http://${matterhorn_installation}/docs.html?path=/files
    // we only expect 200 or 500
    int returnCode = client.executeMethod(delete);
    if (returnCode / 100 != 2) {
      throwException(delete, "deleting media package", dcUrl);
    }
  }

  /**
   * Delete a workflow.
   *
   * <p><code>DELETE /workflow/remove/{id}</code></p>
   *
   * @param workflowId
   * @throws HttpException
   * @throws IOException
   * @throws MediaServiceException
   */
  private void stopWorkflow(String workflowId) throws HttpException, IOException,
      MediaServiceException {
    String dcUrl = "/workflow/stop";

    HttpClient client = new HttpClient();
    PostMethod post = digestAuthed(client, dcUrl, PostMethod.class);
    post.addParameter("id", workflowId);

    // http://${matterhorn_installation}/docs.html?path=/search
    // we only expect 204
    int returnCode = client.executeMethod(post);
    if (returnCode / 100 != 2) {
      throwException(post, "stopping the workflow", dcUrl);
    }
  }

  /**
   * Delete a media package from the search index.
   *
   * <p><code>DELETE /search/{id}</code></p>
   *
   * @param mediaPkgId
   * @throws HttpException
   * @throws IOException
   * @throws MediaServiceException
   */
  private void deleteSearch(String mediaPkgId) throws HttpException, IOException,
      MediaServiceException {
    String dcUrl = String.format("/search/%s", mediaPkgId);

    HttpClient client = new HttpClient();
    DeleteMethod delete = digestAuthed(client, dcUrl, DeleteMethod.class);

    // http://${matterhorn_installation}/docs.html?path=/search
    // we only expect 204 or 500
    int returnCode = client.executeMethod(delete);
    if (returnCode / 100 != 2) {
      throwException(delete, "deleting from search index", dcUrl);
    }
  }

  /**
   * Delete an episode of a media package.
   *
   * @param mediaPkgId
   * @throws HttpException
   * @throws IOException
   * @throws MediaServiceException
   */
  private void deleteEpisode(String mediaPkgId) throws HttpException, IOException,
      MediaServiceException {
    String dcUrl = String.format("/episode/delete/%s", mediaPkgId);

    HttpClient client = new HttpClient();
    DeleteMethod delete = digestAuthed(client, dcUrl, DeleteMethod.class);

    // http://${matterhorn_installation}/docs.html?path=/episode
    // we only expect 204 or 500
    int returnCode = client.executeMethod(delete);
    if (returnCode / 100 != 2) {
      throwException(delete, "deleting episode", dcUrl);
    }
  }

  @Override
  public String[] getPlayerJSUrls(String id) {
    return new String[] {};
  }

  @Override
  public String getPlayerInitJS(String id) {
    return "";
  }

  private void throwException(HttpMethod method, String action, String msg)
      throws MediaServiceException, IOException {
    throw new MediaServiceException("Error while " + action + " [http code="
        + method.getStatusCode() + ";" + msg + "] from Matterhorn: "
        + method.getStatusText() + ": " + method.getResponseBodyAsString());
  }

}
