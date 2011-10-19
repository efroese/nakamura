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
package org.sakaiproject.nakamura.message.listener;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sakaiproject.nakamura.api.message.MessageConstants;

import javax.jcr.Node;
import javax.jcr.Property;

public class MessageRoutesImplTest {

  @Test
  public void testConstructWithNode() throws Exception {
    Property prop = createMock(Property.class);
    expect(prop.getString()).andReturn("smtp:foo@localhost,smtp:bar@localhost");

    Node node = createMock(Node.class);
    expect(node.getProperty(MessageConstants.PROP_SAKAI_TO)).andReturn(prop);
    expect(node.getPath()).andReturn("").anyTimes();
    expect(node.isNew()).andReturn(true).anyTimes();

    replay(node, prop);
    MessageRoutesImpl mri = new MessageRoutesImpl(node);
    assertEquals(2, mri.size());
  }
}
