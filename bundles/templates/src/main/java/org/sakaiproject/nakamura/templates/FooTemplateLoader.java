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
package org.sakaiproject.nakamura.templates;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA. User: zach Date: 1/4/11 Time: 5:00 PM To change this template
 * use File | Settings | File Templates.
 */
public class FooTemplateLoader extends ResourceLoader {
  @Override
  public void init(ExtendedProperties extendedProperties) {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public InputStream getResourceStream(String s) throws ResourceNotFoundException {
    return new ByteArrayInputStream(
        sampleTemplate().getBytes());
  }

  @Override
  public boolean isSourceModified(Resource resource) {
    return false; // To change body of implemented methods use File | Settings | File
                  // Templates.
  }

  @Override
  public long getLastModified(Resource resource) {
    return 0; // To change body of implemented methods use File | Settings | File
              // Templates.
  }

  private String sampleTemplate() {
    return "Dear ${person}, thanks for the fruitcake!";
  }
}
