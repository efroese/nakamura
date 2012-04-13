package org.sakaiproject.nakamura.api.dynamicconfig;

import java.io.IOException;
import java.io.OutputStream;

/**
 * User: duffy
 * Date: Apr 4, 2012
 * Time: 10:10:22 AM
 */
public interface DynamicConfigurationService {

  public String getConfigurationCacheKey() throws DynamicConfigurationServiceException;

  public void writeConfigurationJSON(OutputStream out) throws DynamicConfigurationServiceException;

  public boolean isWriteable();

  public void mergeConfigurationJSON(String json) throws DynamicConfigurationServiceException;
  
}
