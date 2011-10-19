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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.util.Map;

public class MapUtilsTest {

  @Test
  public void testConvertToImmutableMap() {
    String s = "Lorem=ipsum; dolor = sit ;amet=.";
    Map<String, String> m = MapUtils.convertToImmutableMap(s);
    assertTrue(m instanceof ImmutableMap);
    assertEquals("ipsum", m.get("Lorem"));
    assertEquals("sit", m.get("dolor"));
    assertEquals(".", m.get("amet"));
  }
}
