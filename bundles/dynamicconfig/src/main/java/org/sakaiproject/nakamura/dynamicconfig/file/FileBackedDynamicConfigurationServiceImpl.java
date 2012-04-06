package org.sakaiproject.nakamura.dynamicconfig.file;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.sakaiproject.nakamura.api.dynamicconfig.DynamicConfigurationService;
import org.sakaiproject.nakamura.api.dynamicconfig.DynamicConfigurationServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.UUID;

/**
 * User: duffy
 * Date: Apr 4, 2012
 * Time: 10:13:16 AM
 */
@Service
@Component(immediate = true, metatype=true)
@Properties(value = {
    @Property(name = FileBackedDynamicConfigurationServiceImpl.CONFIG_DIR,
       value = FileBackedDynamicConfigurationServiceImpl.DFT_CONFIG_DIR),
    @Property(name = FileBackedDynamicConfigurationServiceImpl.CONFIG_FILENAME,
       value = FileBackedDynamicConfigurationServiceImpl.DFT_CONFIG_FILENAME),
    @Property(name = FileBackedDynamicConfigurationServiceImpl.CUSTOM_DIR,
       value = FileBackedDynamicConfigurationServiceImpl.DFT_CUSTOM_DIR)
})
public class FileBackedDynamicConfigurationServiceImpl implements DynamicConfigurationService {

  /**
   * default log
   */
  private final Logger log = LoggerFactory.getLogger(getClass());

  private static final int CHUNK_SIZE = 8192;

  public static final String CONFIG_DIR = "config.dir";
  public static final String CONFIG_FILENAME = "config.filename";
  public static final String CUSTOM_DIR = "custom.dir";
  public static final String DFT_CONFIG_DIR = "var/dynamicconfig";
  public static final String DFT_CUSTOM_DIR = "var/dynamicconfig/custom";
  public static final String DFT_CONFIG_FILENAME = "config.json";

  protected String configurationDir;
  protected String configurationFilename;
  protected String customDir;

  protected static MessageDigest MD5;

  public FileBackedDynamicConfigurationServiceImpl () {
    try {
      MD5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {

      // there is something really wrong if we get here - log the error and return a random UUID

      log.error("failed to obtain MessageDigest object to compute MD5 sum", e);
      log.warn ("unique configuration cache key will be used for every request - no caching is being preformed for config");

      MD5 = null;
    }
  }

  private final File getConfigurationFile() {
    final StringBuilder computedPath = new StringBuilder();

    if (configurationDir.trim().length() > 0) {
      computedPath.append(configurationDir);
      if (!configurationDir.endsWith("/")) {
        computedPath.append("/");
      }
    }

    computedPath.append(configurationFilename);

    return new File(computedPath.toString());
  }

  private final File getCustomDir() {
    if (customDir == null) {
      return null;
    }

    return new File(customDir);
  }

  protected void activate (ComponentContext componentContext) throws Exception {
    @SuppressWarnings("unchecked")
    final Dictionary<String, Object> props = componentContext.getProperties();

    configurationDir = OsgiUtil.toString(props.get(CONFIG_DIR), DFT_CONFIG_DIR);
    configurationFilename = OsgiUtil.toString(props.get(CONFIG_FILENAME), DFT_CONFIG_FILENAME);
    customDir = OsgiUtil.toString(props.get(CUSTOM_DIR), DFT_CUSTOM_DIR);

    final File configFile = getConfigurationFile();

    if (!(configFile.exists() && configFile.canRead())) {
      log.error ("config file is unreadable: [" + configFile.getAbsolutePath() + "]");
    }

    final File customDir = getCustomDir();

    if (customDir != null && customDir.exists()) {
      if (!customDir.canRead()) {
        log.error("custom configuration directory is unreadable: [" + customDir.getAbsolutePath() + "]");
      }
    } else {
      log.warn("custom configuration directory does not exist - no custom configurations will be applied");
    }
  }

  protected void deactivate (ComponentContext context) throws Exception {
    configurationDir = null;
    configurationFilename = null;
    customDir = null;
  }

  protected final JSONObject collectConfiguration (File jsonFile)
     throws IOException, FileNotFoundException, JSONException {

    if (jsonFile == null) {
      log.error("attempt to process null configuration file aborted");
      //return an empty JSONObject
      return new JSONObject();
    }

    if (!jsonFile.canRead()) {
      log.error("attempt to process empty configuration file aborted");
      //return an empty JSONObject
      return new JSONObject();
    }

    final FileReader reader = new FileReader(jsonFile);

    final StringBuffer buffer = new StringBuffer();
    char[] chunk = new char[CHUNK_SIZE];
    int read = 0;

    try {
      while ( (read = reader.read(chunk)) > 0) {
        buffer.append(chunk, 0, read);
      }
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          log.error("error closing file [" + jsonFile.getAbsolutePath() + "]", e);
        }
      }
    }

    return new JSONObject(buffer.toString());
  }

  protected final JSONObject collectConfiguration ()
     throws IOException, JSONException, DynamicConfigurationServiceException {
    final JSONObject config;

    //first read the main configuration
    final File masterConfigFile = getConfigurationFile();

    if (masterConfigFile == null) {
      log.error("no master configuration file supplied - aborting configuration");
      throw new DynamicConfigurationServiceException("Configuration file is null");
    }

    config = collectConfiguration(masterConfigFile);

    //process the custom configuration
    final File customConfigDir = getCustomDir();
    File[] files = null;

    if (customDir != null){
      files = customConfigDir.listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File file, String s) {
            return s.endsWith(".json");
          }
        }
      );
    }

    if (files == null || files.length == 0) {
      return config;
    }

    JSONObject nextConfig;

    for (final File file : files)
    {
      try {
        nextConfig = collectConfiguration(file);

        Iterator<String> keyIt = nextConfig.keys();
        while (keyIt.hasNext()) {
          final String key = keyIt.next();
          config.put (key, nextConfig.get(key));
        }
      } catch (Exception e) {
        log.error("failed to process custom configuration file [" + file.getAbsolutePath() + "]", e);
      }
    }

    return config;
  }

  @Override
  public String getConfigurationCacheKey() throws DynamicConfigurationServiceException {

    JSONObject config = null;

    try {
      config = collectConfiguration();
    } catch (Exception e) {
      throw new DynamicConfigurationServiceException("error calculating cache key", e);
    }

    String key = null;

    if (MD5 != null) {
      MD5.update(config.toString().getBytes());
      byte[] md5sum = MD5.digest();
      BigInteger bigInt = new BigInteger(1, md5sum);
      key = bigInt.toString(16);
    }

    if (key == null) {
      key = UUID.randomUUID().toString();
      log.warn ("error creating cache key - using random UUID");
    }

    log.debug ("configuration cache key: " + key);

    return key;
  }

  @Override
  public void writeConfigurationJSON(OutputStream out) throws DynamicConfigurationServiceException {
    try {
      JSONObject config = collectConfiguration();

      out.write(config.toString().getBytes());
    } catch (Exception e) {
      throw new DynamicConfigurationServiceException("failed to write configuration to stream", e);
    }
  }

  @Override
  public boolean isWriteable() {
    return false;
  }

  @Override
  public void mergeConfigurationJSON(final String json) throws DynamicConfigurationServiceException {
    throw new DynamicConfigurationServiceException("configuration is not writeable");
  }
}