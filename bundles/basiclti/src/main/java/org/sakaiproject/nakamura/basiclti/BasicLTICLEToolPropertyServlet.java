package org.sakaiproject.nakamura.basiclti;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.sakaiproject.nakamura.util.ExtendedJSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Dictionary;

import javax.servlet.ServletException;

@SlingServlet(methods = { "POST", "GET" }, generateService = true, paths = { "/var/basiclti/cletool" })
public class BasicLTICLEToolPropertyServlet extends SlingAllMethodsServlet {

  @Property(value = "http://localhost")
  protected static final String CLE_SERVER_URL = "sakai.cle.server.url";

  @Property(value = "12345")
  protected static final String CLE_BASICLTI_KEY = "sakai.cle.basiclti.key";

  @Property(value = "secret")
  protected static final String CLE_BASICLTI_SECRET = "sakai.cle.basiclti.secret";

  @Property(longValue = 100)
  protected static final String CLE_BASICLTI_FRAME_HEIGHT = "sakai.cle.basiclti.frame.height";

  @Property(boolValue = true)
  protected static final String LTI_URL_LOCK = "sakai.cle.basiclti.url.lock";

  @Property(boolValue = true)
  protected static final String LTI_KEY_LOCK = "sakai.cle.basiclti.key.lock";

  @Property(boolValue = true)
  protected static final String LTI_SECRET_LOCK = "sakai.cle.basiclti.secret.lock";

  @Property(boolValue = true)
  protected static final String LTI_RELEASE_NAMES = "sakai.cle.basiclti.release.names";

  @Property(boolValue = true)
  protected static final String LTI_RELEASE_NAMES_LOCK = "sakai.cle.basiclti.release.names.lock";

  @Property(boolValue = true)
  protected static final String LTI_RELEASE_EMAIL = "sakai.cle.basiclti.release.email";

  @Property(boolValue = true)
  protected static final String LTI_RELEASE_EMAIL_LOCK = "sakai.cle.basiclti.release.email.lock";

  @Property(boolValue = true)
  protected static final String LTI_RELEASE_PRINCIPAL = "sakai.cle.basiclti.release.principal";

  @Property(boolValue = true)
  protected static final String LTI_RELEASE_PRINCIPAL_LOCK = "sakai.cle.basiclti.release.principal.lock";

  @Property(boolValue = false)
  protected static final String LTI_DEBUG = "sakai.cle.basiclti.debug";

  @Property(value = { "sakai.announcements", "sakai.postem", "sakai.profile2",
      "sakai.assignment.grades", "sakai.profile", "sakai.chat", "sakai.resources",
      "sakai.dropbox", "sakai.rwiki", "sakai.forums", "sakai.samigo",
      "sakai.gradebook.tool", "sakai.schedule", "sakai.mailbox", "sakai.singleuser",
      "sakai.messages", "sakai.site.roster", "sakai.news", "sakai.summary.calendar",
      "sakai.poll", "sakai.syllabus" })
  protected static final String TOOL_LIST = "sakai.cle.basiclti.tool.list";

  private String cleUrl;
  private String ltiKey;
  private String ltiSecret;
  private Long frameHeight;
  private Boolean urlLock;
  private Boolean keyLock;
  private Boolean secretLock;
  private Boolean releaseNames;
  private Boolean releaseNamesLock;
  private Boolean releaseEmail;
  private Boolean releaseEmailLock;
  private Boolean releasePrincipal;
  private Boolean releasePrincipalLock;
  private Boolean ltiDebug;
  private String[] toolList;

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory
      .getLogger(BasicLTICLEToolPropertyServlet.class);

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {
    getTool(request, response, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.sling.api.servlets.SlingAllMethodsServlet#doPost(org.apache.sling.api.SlingHttpServletRequest,
   *      org.apache.sling.api.SlingHttpServletResponse)
   */
  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {
    getTool(request, response, true);
  }

  private void getTool(SlingHttpServletRequest request,
      SlingHttpServletResponse response, boolean b) throws IOException {
    ExtendedJSONWriter ejw = new ExtendedJSONWriter(response.getWriter());
    try {
      if (request.getRequestParameter("list") != null) {
        ejw.object();
        ejw.key("toolList").value(toolList);
        ejw.endObject();
      } else {
        ejw.object();
        ejw.key("ltiurl").value(
            cleUrl + "/imsblti/provider/" + request.getRequestParameter("t"));
        ejw.key("ltiurl_lock").value(urlLock);
        ejw.key("ltikey_lock").value(keyLock);
        ejw.key("ltisecret_lock").value(secretLock);
        ejw.key("release_names").value(releaseNames);
        ejw.key("release_names_lock").value(releaseNamesLock);
        ejw.key("frame_height").value(frameHeight);
        ejw.key("release_email").value(releaseEmail);
        ejw.key("release_email_lock").value(releaseEmailLock);
        ejw.key("release_principal_name").value(releasePrincipal);
        ejw.key("release_principal_name_lock").value(releasePrincipalLock);
        ejw.key("debug").value(ltiDebug);
        ejw.key("ltiKeys").object();
        ejw.key("ltikey").value(ltiKey);
        ejw.key("ltisecret").value(ltiSecret);
        ejw.endObject();
        ejw.endObject();
      }
    } catch (JSONException e) {
      final Writer trace = new StringWriter();
      final PrintWriter pw = new PrintWriter(trace);
      e.printStackTrace(pw);
      response.sendError(500, trace.toString());
    }
  }

  @Activate
  protected void activate(ComponentContext componentContext) throws Exception {
    Dictionary<?, ?> properties = componentContext.getProperties();
    LOG.info("Starting Activation of Hybrid BasicLTI CLE tool property servlet");
    cleUrl = OsgiUtil.toString(properties.get(CLE_SERVER_URL), "http://localhost");
    ltiKey = OsgiUtil.toString(properties.get(CLE_BASICLTI_KEY), "12345");
    ltiSecret = OsgiUtil.toString(properties.get(CLE_BASICLTI_SECRET), "secret");
    frameHeight = OsgiUtil.toLong(properties.get(CLE_BASICLTI_FRAME_HEIGHT), 100);
    urlLock = OsgiUtil.toBoolean(properties.get(LTI_URL_LOCK), true);
    keyLock = OsgiUtil.toBoolean(properties.get(LTI_KEY_LOCK), true);
    secretLock = OsgiUtil.toBoolean(properties.get(LTI_SECRET_LOCK), true);
    releaseNames = OsgiUtil.toBoolean(properties.get(LTI_RELEASE_NAMES), true);
    releaseNamesLock = OsgiUtil.toBoolean(properties.get(LTI_RELEASE_NAMES_LOCK), true);
    releaseEmail = OsgiUtil.toBoolean(properties.get(LTI_RELEASE_EMAIL), true);
    releaseEmailLock = OsgiUtil.toBoolean(properties.get(LTI_RELEASE_EMAIL_LOCK), true);
    releasePrincipal = OsgiUtil.toBoolean(properties.get(LTI_RELEASE_PRINCIPAL), true);
    releasePrincipalLock = OsgiUtil.toBoolean(properties.get(LTI_RELEASE_PRINCIPAL_LOCK),
        true);
    ltiDebug = OsgiUtil.toBoolean(properties.get(LTI_DEBUG), false);
    toolList = OsgiUtil.toStringArray(properties.get(TOOL_LIST));
  }
}
