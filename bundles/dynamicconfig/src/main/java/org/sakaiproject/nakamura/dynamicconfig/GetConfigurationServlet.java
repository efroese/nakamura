package org.sakaiproject.nakamura.dynamicconfig;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.sakaiproject.nakamura.api.doc.BindingType;
import org.sakaiproject.nakamura.api.doc.ServiceBinding;
import org.sakaiproject.nakamura.api.doc.ServiceDocumentation;
import org.sakaiproject.nakamura.api.doc.ServiceExtension;
import org.sakaiproject.nakamura.api.doc.ServiceMethod;
import org.sakaiproject.nakamura.api.doc.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;

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

  protected void streamConfigurationJS(final OutputStream out)
  {
    final String header = "define(function(){    var config = ";
    final String footer = ";    return config;});";

    try {
      out.write(header.getBytes());
      streamConfigurationJSON(out);
      out.write(footer.getBytes());
    } catch (IOException e) {
      log.error ("error occurred streaming configuration JS", e);
    }
  }

  protected void streamConfigurationJSON(final OutputStream out)
  {
    final String testData = "{ 'test': 'value' }";

    try {
      out.write(testData.getBytes());
    } catch (IOException e) {
      log.error ("error occurred streaming configuration JSON", e);
    }
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
     throws ServletException, IOException {

    final RequestPathInfo pathInfo = request.getRequestPathInfo();
    final String extension = pathInfo.getExtension();

    if ("json".equals(extension)) {
      response.setStatus(200);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      streamConfigurationJSON(response.getOutputStream());
    } else if ("js".equals(extension)) {
      response.setStatus(200);
      response.setContentType("text/javascript");
      response.setCharacterEncoding("UTF-8");
      streamConfigurationJS(response.getOutputStream());
    } else {
      response.setStatus(400);
    }
  }
}