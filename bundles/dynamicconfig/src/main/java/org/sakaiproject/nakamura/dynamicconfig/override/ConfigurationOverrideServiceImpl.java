package org.sakaiproject.nakamura.dynamicconfig.override;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.ComponentContext;
import org.sakaiproject.nakamura.api.dynamicconfig.ConfigurationOverrideService;
import org.sakaiproject.nakamura.util.IOUtils;
import org.sakaiproject.nakamura.util.JcrUtils;
import org.sakaiproject.nakamura.util.NodeInputStream;
import org.sakaiproject.nakamura.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Service
@Component(immediate = true, metatype=true)
public class ConfigurationOverrideServiceImpl implements ConfigurationOverrideService {
  /**
   * default log
   */
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Property( value = {"org.sakaiproject.nakamura.uxloader:var/dynamicconfig/override/uxloader"},
     description="location of configuration override files" )
  public static final String OVERRIDE_DIRS = "override.dirs";

  private static final String LAST_MODIFIED   = "jcr:lastModified";
  private static final String CONTENT         = "jcr:content";
  private static final String DATA            = "jcr:data";

  @Reference
  protected transient SlingRepository jcrRepository;
  protected transient Map<String, File> overrideDirectories;

  @Activate
  public void activate(final ComponentContext componentContext) {
    @SuppressWarnings("unchecked")
    final Dictionary props = componentContext.getProperties();
    final BundleContext bundleContext = componentContext.getBundleContext();
    final HashMap<String, Bundle> bundlesByName = new HashMap<String, Bundle>();
    final String[] overrides = PropertiesUtil.toStringArray(props.get(OVERRIDE_DIRS));

    final Bundle bundles[] = bundleContext.getBundles();

    for (Bundle bundle : bundles) {
      bundlesByName.put(bundle.getSymbolicName(), bundle);
    }
    overrideDirectories = new HashMap<String, File>();

    if (overrides != null) {
      for (final String override : overrides) {
        final String[] overrideDef = StringUtils.split(override, ':');
        final String bundleName = overrideDef[0];
        final File directory = new File (overrideDef[1]);

        overrideDirectories.put(bundleName, directory);

        final Bundle overriddenBundle = bundlesByName.get(bundleName);

        if (overriddenBundle != null) {
          processOverrides(overriddenBundle);
        }
      }
    }

    componentContext.getBundleContext().addBundleListener( new DependencyLoadCheck() );
  }

  private final String relativePath (final String root, final String toConvert) {
    return toConvert.replace(root, "");
  }

  private final Node getNode (final Session session, final String rootPath, final File file)
     throws RepositoryException {
    final String filePath = file.getPath();
    String relPath = relativePath(rootPath, filePath);
    Node node = null;

    if (relPath.length() == 0) {
      relPath = "/";
    }

    return session.getNode(relPath);
  }

  private final File[] children (final File directory) {
    return directory.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File file, String s) {
        return (!".".equals(s) && !"..".equals(s));
      }
    });
  }

  protected void processOverrides (final Bundle bundle) {
    final String bundleName = bundle.getSymbolicName();
    /*
       only need to override resources for bundles that have started (and therefore have done their imports) and
       for which we have an override directory registered.
     */
    if (Bundle.ACTIVE == bundle.getState() && overrideDirectories.containsKey(bundleName)) {
      final File overrideDir = overrideDirectories.get(bundleName);

      if (!overrideDir.exists()) {
        log.info("no configuraiton directory {} exists - no overrides processed for {}",
           new Object[] { overrideDir, bundleName });
        return;
      }
      if (!overrideDir.canRead()) {
        log.error("cannot read configuration directory {} to process overrides for {}",
           new Object[] { overrideDir, bundleName });
        return;
      }

      //store the path so we can construct relative paths - the should match the layout of resources in JCR
      String overridePath = overrideDir.getPath();

      //terminate the path with '/'
      while (overridePath.endsWith("/") && overridePath.length() > 0) {
        overridePath = overridePath.substring(0, overridePath.length() - 1);
      }

      //using a stack to do a breadth-first traversal of the directories under override dir
      final Stack<File> directories = new Stack<File>();
      directories.push(overrideDir);

      //log in administratively to JCR
      final Session session;
      try {
        session = jcrRepository.loginAdministrative(null);
      } catch (RepositoryException e) {
        log.error ("Cannot access JCR Repository, aborting configuration override", e);
        return;
      }

      try {
        // process the override dir content until it is exhausted
        while (!directories.isEmpty()) {
          // pop item - it should be a directory
          final File nextDir = directories.pop();

          // get node corresponding to the directory
          final Node dirNode;

          try {
            dirNode = getNode (session, overridePath, nextDir);
          } catch (RepositoryException e) {
            log.error("Could not obtain JCR node for directory [" + overridePath +"] - skipping override", e);
            continue;
          }

          // iterate through children
          File children[] = children(nextDir);

          for (File child : children) {
            final String childName = child.getName();
            final Node childNode;

            try {
              if (child.isDirectory()) {
                // if child is a directory, ensure node exists and push
                childNode = getOrCreateNode(dirNode, childName, "nt:folder");
                directories.push(child);
              } else {
                // if child is a file, ensure node exists and process file
                childNode = getOrCreateNode(dirNode, childName, "nt:file");

                if (!uptodate (childNode, child)) {
                  if ( childName.endsWith(".json") ) {
                    mergeJsonFile (childNode, child);
                  } else if ( childName.endsWith(".properties") ) {
                    mergePropertiesFile (childNode, child);
                  } else {
                    overwriteFileContent(childNode, child);
                  }
                  session.save();
                }
              }
            } catch (Exception e) {
              log.error ("Could not update override configuration for path [" + overridePath + "]", e);
            }
          }
        }
      } finally {
        session.logout();
      }
    }
  }

  private final boolean uptodate(final Node childNode, final File child) {

    Calendar nodeModDate = null;
    try {
      final Node contentNode = childNode.getNode(CONTENT);
      nodeModDate = contentNode.getProperty(LAST_MODIFIED).getDate();
    } catch (Exception e) {
      log.error("error getting information on existing content for override file " + child.getPath()
         + " assuming content is out of date", e);

      return false;
    }

    long fileModMillis = child.lastModified();

    return (nodeModDate.getTimeInMillis() > fileModMillis);
  }

  protected final Node getOrCreateNode(final Node parent, final String childName, final String type)
     throws RepositoryException {
    try {
      return parent.getNode(childName);
    } catch (PathNotFoundException e) {
      log.info("creating node {} during override at {}", new Object[] {childName, parent.getPath()});
      return parent.addNode(childName, type);
    }
  }

  protected Node updateFile (final Node fileNode, final InputStream iStream, final String mimeType,
     final String encoding) throws RepositoryException, IOException
  {
      //create the mandatory child node - jcr:content
      Node resNode = getOrCreateNode(fileNode, CONTENT, "nt:resource");
      resNode.setProperty ("jcr:mimeType", mimeType);
      resNode.setProperty ("jcr:encoding", encoding);
      Binary binary = resNode.getSession().getValueFactory().createBinary(iStream);
      resNode.setProperty("jcr:data", binary);
      Calendar lastModified = Calendar.getInstance ();
      resNode.setProperty ("jcr:lastModified", lastModified);

      return fileNode;
  }

  protected void mergeJsonFile (final Node node, final File file) throws RepositoryException,
     IOException, JSONException {
    NodeInputStream nodeIS = JcrUtils.getInputStreamForNode(node);
    final String jsonStr = IOUtils.readFully(nodeIS.getInputStream(), "UTF-8");
    final JSONObject jcrContent = new JSONObject (jsonStr);
    overwriteFileContent(node, file);

/*
    final InputStream fileIS = new FileInputStream(file);
    final String fileStr = IOUtils.readFully(fileIS, "UTF-8");
    final JSONObject fileContent = new JSONObject (fileStr);

    updateFile (node, fileIS, file.getName(), "application/json", "UTF-8");
*/
  }

  protected void mergePropertiesFile (final Node node, final File file) throws RepositoryException, IOException {
    overwriteFileContent(node, file);
  }

  private void overwriteFileContent(Node node, File file) throws RepositoryException, IOException {
    final InputStream fileIS = new FileInputStream(file);

    updateFile (node, fileIS, "application/json", "UTF-8");
  }

  protected class DependencyLoadCheck implements BundleListener {
    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
      if (BundleEvent.STARTED == bundleEvent.getType()) {
        processOverrides(bundleEvent.getBundle());
      }
    }
  }
}
