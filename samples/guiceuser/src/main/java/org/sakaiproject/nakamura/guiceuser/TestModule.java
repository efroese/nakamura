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

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import org.sakaiproject.nakamura.guiceuser.api.InterfaceA;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceB;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceD;
import org.sakaiproject.nakamura.guiceuser.api.InterfaceE;
import org.sakaiproject.nakamura.guiceuser.impl.ConcreteA;
import org.sakaiproject.nakamura.guiceuser.impl.ConcreteB;
import org.sakaiproject.nakamura.guiceuser.impl.ConcreteD;
import org.sakaiproject.nakamura.guiceuser.impl.ConcreteE;
import org.sakaiproject.nakamura.guiceuser.impl.FProvider;

public class TestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(InterfaceA.class).to(ConcreteA.class).in(Scopes.SINGLETON);
    bind(InterfaceB.class).to(ConcreteB.class).in(Scopes.SINGLETON);
    bind(InterfaceD.class).to(ConcreteD.class).in(Scopes.SINGLETON);
    bind(InterfaceE.class).to(ConcreteE.class).in(Scopes.SINGLETON);
    bind(FProvider.class);
  }

}
