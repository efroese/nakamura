package org.sakaiproject.nakamura.api.dynamicconfig;

/**
 * User: duffy
 * Date: Apr 4, 2012
 * Time: 10:53:48 AM
 */
public class DynamicConfigurationServiceException extends Exception {
  public DynamicConfigurationServiceException() {
    super();
  }

  public DynamicConfigurationServiceException(String s) {
    super(s);
  }

  public DynamicConfigurationServiceException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public DynamicConfigurationServiceException(Throwable throwable) {
    super(throwable);
  }
}
