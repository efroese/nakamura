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
package org.sakaiproject.nakamura.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.util.Enumeration;

public class UrlEnumerationTest {

  @Test
  public void testUrlEnumeration() throws Exception {
    Enumeration<URL> eu = new UrlEnumeration(new URL("http://example.com"));
    assertTrue(eu.hasMoreElements());
    assertEquals(new URI("http://example.com"), eu.nextElement().toURI());
    assertFalse(eu.hasMoreElements());
    assertNull(eu.nextElement());
  }
}
