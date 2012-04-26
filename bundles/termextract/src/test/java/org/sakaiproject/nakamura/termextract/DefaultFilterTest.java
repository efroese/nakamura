/*
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
package org.sakaiproject.nakamura.termextract;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultFilterTest {
  DefaultFilter filter;

  @Before
  public void setUp() {
    filter = new DefaultFilter(2, 2, 4, 2, 12);
  }

  @Test
  public void testMinLength() {
    Assert.assertFalse(filter.filter("t", 2, 2));
    Assert.assertTrue(filter.filter("tt", 2, 2));
  }
  
  @Test
  public void testMaxLength() {
    Assert.assertFalse(filter.filter("tttttttttttttt", 2, 2));
    Assert.assertTrue(filter.filter("tt", 2, 2));
  }

  @Test
  public void testMinStrength() {
    Assert.assertFalse(filter.filter("tt", 2, 1));
    Assert.assertTrue(filter.filter("tt tt tt", 2, 3));
  }
  
  @Test
  public void testMaxStrength() {
    Assert.assertFalse(filter.filter("tt tt tt tt tt", 2, 5));
    Assert.assertTrue(filter.filter("tt qq", 2, 2));
  }

  @Test
  public void testSingleStrengthMinOccurrence() {
    Assert.assertFalse(filter.filter("tt", 1, 1));
    Assert.assertTrue(filter.filter("tt", 1, 2));
  }
}
