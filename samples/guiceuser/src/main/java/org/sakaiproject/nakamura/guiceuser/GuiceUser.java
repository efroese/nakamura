/**
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
package org.sakaiproject.nakamura.guiceuser;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceD;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceE;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceF;
import org.sakaiproject.nakamura.guiceuser.impl.FProvider;
import org.sakaiproject.nakamura.guiceuser.impl.InjectableC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiceUser implements BundleActivator {

  private static final Logger LOGGER = LoggerFactory.getLogger(GuiceUser.class);

  public void start(BundleContext arg0) throws Exception {
    LOGGER.info("Fetching injector");
    Injector injector = Guice.createInjector(new TestModule());
    LOGGER.info("Fetching Injectable instance");
    injector.getInstance(InjectableC.class);
    LOGGER.info("Print test 1");
    InterfaceD d = injector.getInstance(InterfaceD.class);
    d.printHello();
    InterfaceE e = injector.getInstance(InterfaceE.class);
    LOGGER.info("Print test 2");
    e.printHelloViaD();
    Provider<InterfaceF> fprovider = injector.getInstance(FProvider.class);
    LOGGER.info("Print test 3");
    fprovider.get().printViaE();
  }

  public void stop(BundleContext arg0) throws Exception {
    LOGGER.info("Stopping");
  }

}
