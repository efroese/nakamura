package org.sakaiproject.nakamura.dynamicconfig;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.sakaiproject.nakamura.api.doc.BindingType;
import org.sakaiproject.nakamura.api.doc.ServiceBinding;
import org.sakaiproject.nakamura.api.doc.ServiceDocumentation;
import org.sakaiproject.nakamura.api.doc.ServiceExtension;
import org.sakaiproject.nakamura.api.doc.ServiceMethod;
import org.sakaiproject.nakamura.api.doc.ServiceResponse;
import org.sakaiproject.nakamura.api.dynamicconfig.DynamicConfigurationService;
import org.sakaiproject.nakamura.api.dynamicconfig.DynamicConfigurationServiceException;
import org.sakaiproject.nakamura.util.ExtendedJSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * User: duffy
 * Date: Apr 3, 2012
 * Time: 4:27:47 PM
 */
@SlingServlet(paths={"/system/dynamic-config/config"},resourceTypes={"oae/configuration"},
   methods={"GET"},extensions={"json","js"})
@ServiceDocumentation(name="Get Configuration Servlet", okForVersion = "1.1",
    description="Returns the server configuration as either a require.js module, or as a JSON object " +
        "based on the extension provided in the GET request (eg. '.js' or '.json)",
    shortDescription="Get the server configuration as json or js",
    bindings=@ServiceBinding(type= BindingType.TYPE,bindings={"oae/configuration"},
        extensions=@ServiceExtension(name="json", description="")),
    methods=@ServiceMethod(name="GET",
        description={"Get the configuration as a require.js module or as json.",
            "Examples:<br>" +
            "<pre>curl http://localhost:8080/system/config.json</pre><br>" +
            "<pre>curl http://localhost:8080/system/config.js</pre>"},
        response={
          @ServiceResponse(code=200,description="Success"),
          @ServiceResponse(code=500,description="Failure with HTML explanation.")}
   ))
public class GetConfigurationServlet extends SlingAllMethodsServlet {

  /**
   * default log
   */
  private final Logger log = LoggerFactory.getLogger(getClass());

  public static final String DFT_CALLBACK = "define";

  @Reference
  protected DynamicConfigurationService configurationService;

  protected void streamConfigurationCacheKeyJS (final String callback, final Writer writer) throws Exception {
    final String header = "(function(){\n\tvar configurationMetaData = ";
    final String footer = ";\n\treturn configurationMetaData;\n});";

    try {
      writer.write(callback);
      writer.write(header);
      streamConfigurationCacheKeyJSON(writer);
      writer.write(footer);
    } catch (IOException e) {
      log.error ("error occurred streaming configuration JS", e);
    }
  }

  protected void streamConfigurationCacheKeyJS(final Writer out) throws Exception {
    streamConfigurationCacheKeyJS(DFT_CALLBACK, out);
  }

  protected void streamConfigurationCacheKeyJSON (final Writer writer) throws Exception {
    final String cacheKey = configurationService.getConfigurationCacheKey();
    final Map<String, Object> valueMap = new HashMap<String, Object>();
    final ExtendedJSONWriter jsonWriter = new ExtendedJSONWriter(writer);

    valueMap.put("configurationCacheKey", cacheKey);
    jsonWriter.valueMap(valueMap);
  }

  protected void streamConfigurationJS(final String callback, final OutputStream out) throws Exception {
    final String header = "(function(){\n\tvar config = ";
    final String footer = ";\n\treturn config;\n});";

    try {
      out.write(callback.getBytes());
      out.write(header.getBytes());
      streamConfigurationJSON(out);
      out.write(footer.getBytes());
    } catch (IOException e) {
      log.error ("error occurred streaming configuration JS", e);
    }
  }

  protected void streamConfigurationJS(final OutputStream out) throws Exception {
    streamConfigurationJS(DFT_CALLBACK, out);
  }

  protected void streamConfigurationJSON(final OutputStream out) throws Exception {
    if (configurationService == null) {
      log.error("dynamic configuration service not configured");
      throw new DynamicConfigurationServiceException("dynamic configuration service not configured");
    }

    configurationService.writeConfigurationJSON(out);
  }

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
     throws ServletException, IOException {

    if (!configurationService.isWriteable()) {
      log.error("an attempt was made to POST configuration JSON but the configuration is unwriteable");
      response.sendError(405, "configuration is not writeable");
      return;
    }

    final RequestParameter json = request.getRequestParameter("json");

    try {
      configurationService.mergeConfigurationJSON(json.getString());
    } catch (DynamicConfigurationServiceException e) {
      log.error("failed to merge configuration JSON", e);
      response.sendError(500, "failed to merge configuration JSON");
    }

    response.setStatus(200);

    if ("json".equals(request.getRequestPathInfo().getExtension())) {
      Map<String, Object> valueMap = new HashMap<String, Object>();
      valueMap.put("success", "true");
      ExtendedJSONWriter writer = new ExtendedJSONWriter(response.getWriter());
      writer.valueMap(valueMap);
    }
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
     throws ServletException, IOException {

    final RequestPathInfo pathInfo = request.getRequestPathInfo();
    final String extension = pathInfo.getExtension();
    final String selectors[] = pathInfo.getSelectors();

    boolean sendCacheKey = false;

    if (selectors != null && selectors.length > 0) {
      for (String selector : selectors) {
        if ("cachekey".equals(selector)) {
          sendCacheKey = true;
        }
        /* process other selectors here
        else if (...) {
        }
         */
      }
    }

    if ("json".equals(extension)) {
      response.setStatus(200);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      final RequestParameter callback = request.getRequestParameter("callback");

      try {
        if (callback != null) {
          if (sendCacheKey) {
            streamConfigurationCacheKeyJS(callback.getString(), response.getWriter());
          } else {
            streamConfigurationJS(callback.getString(), response.getOutputStream());
          }
        } else {
          if (sendCacheKey) {
            streamConfigurationCacheKeyJSON(response.getWriter());
          } else {
            streamConfigurationJSON(response.getOutputStream());
          }
        }
      } catch (Exception e) {
        log.error ("failed to stream JSON for dynamic configuration", e);
        response.setStatus(500);
      }
    } else if ("js".equals(extension)) {

      response.setStatus(200);
      response.setContentType("text/javascript");
      response.setCharacterEncoding("UTF-8");
      try {
        if (sendCacheKey) {
          streamConfigurationCacheKeyJS(response.getWriter());
        } else {
          streamConfigurationJS(response.getOutputStream());
        }
      } catch (Exception e) {
        log.error ("failed to stream JS for dynamic configuration", e);
        response.setStatus(500);
      }
    } else {
      response.setStatus(400);
    }
  }
}