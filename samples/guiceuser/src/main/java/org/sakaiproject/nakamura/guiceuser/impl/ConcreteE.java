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
package org.sakaiproject.nakamura.guiceuser.impl;

import com.google.inject.Inject;

import org.sakaiproject.nakamura.guiceuser.api.InterfaceA;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceB;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceD;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcreteE implements InterfaceE {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(ConcreteE.class);
  private InterfaceD d;
  
  @Inject
  public ConcreteE(InterfaceD d)
  {
    this.d = d;
  }
  
  public void printHelloViaD()
  {
    this.d.printHello();
  }
  
  public void doPrint(InterfaceA a, InterfaceB b)
  {
    LOGGER.info("Printing from e");
    b.printString(a.getHelloWorld());
  }


}
