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
package org.sakaiproject.nakamura.pages.search;

import junit.framework.Assert;

import static org.mockito.Mockito.when;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResourceResolverUnicodePathTest {
  static final String utf8Path = "/utf-βετα-5/pages/_pages/";
  static final String ISO88591Path = "/~utf-Î²ÎµÏÎ±-5/pages/_pages/";
  
  @Mock
  SlingHttpServletRequest request;
  
  @Mock
  RequestParameter requestParameter;

  @Mock
  ResourceResolver resourceResolver;

  @Mock
  Resource pagesResource;
  
  @Test
  /**
   * see KERN-1759
   */
  public void test() throws Exception {
    when(request.getRequestParameter("path")).thenReturn(requestParameter);
    when(requestParameter.getString("UTF-8")).thenReturn(utf8Path);
    when(requestParameter.getString()).thenReturn(ISO88591Path);
    when(request.getResourceResolver()).thenReturn(resourceResolver);
    when(resourceResolver.getResource(utf8Path)).thenReturn(pagesResource);
    when(resourceResolver.getResource(ISO88591Path)).thenReturn(null);
    
    requestParameter = request.getRequestParameter("path");
    resourceResolver = request.getResourceResolver();
    String goodPath = requestParameter.getString("UTF-8");
    String badPath = requestParameter.getString();
    
    pagesResource = resourceResolver.getResource(goodPath);
    Assert.assertNotNull(pagesResource);
    
    pagesResource = resourceResolver.getResource(badPath);
    Assert.assertNull(pagesResource);
  }
}
