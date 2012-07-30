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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.nakamura.api.termextract.ExtractedTerm;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TermExtractorTest {
  TermExtractorImpl extractor;
  TaggerImpl tagger;
  
  @Before
  public void setUp() {
    tagger = new TaggerImpl();
    tagger.activate();
    extractor = new TermExtractorImpl(tagger);
  }
  
  @Test
  public void testExtraction() throws Exception {
    String txt = TermExtractUtil.readExampleText();
    List<ExtractedTerm> terms = extractor.process(txt);
<<<<<<< HEAD
<<<<<<< HEAD
    System.out.println("extracted: " + terms);
=======
    System.out.println(terms);
>>>>>>> Port of the topia termextractor from Python.
=======
    System.out.println("extracted: " + terms);
>>>>>>> Add fixes from github repo (https://github.com/turian/topia.termextract)
  }
}
