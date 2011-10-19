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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class IOUtilsTest {

  @Test
  public void testReadFully() throws Exception {
    InputStream is = this.getClass().getResourceAsStream("lipsum.txt");
    assertNotNull(is);
    String lipsum = IOUtils.readFully(is, "UTF-8");
    InputStream verify = this.getClass().getResourceAsStream("lipsum.txt");
    for (Byte b : lipsum.getBytes()) {
      assertEquals(b.intValue(), verify.read());
    }
    verify.close();
  }

  @Test
  public void testStream() throws Exception {
    InputStream from = this.getClass().getResourceAsStream("lipsum.txt");

    ByteArrayOutputStream to = new ByteArrayOutputStream();
    IOUtils.stream(from, to);
    String lipsum = to.toString("UTF-8");
    assertEquals("Lorem", lipsum.substring(0, 5));

  }
}
