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

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.nakamura.api.termextract.TaggedTerm;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TaggerTest {
  TaggerImpl tagger;
  
  @Before
  public void setUp() {
    tagger = new TaggerImpl();
  }
  
  @Test
  public void testTagging() throws Exception {
    String txt = TermExtractUtil.readExampleText();
    List<TaggedTerm> terms = tagger.process(txt);
    Collections.sort(terms, new Comparator<TaggedTerm>() {
      Collator collator = Collator.getInstance();

      @Override
      public int compare(TaggedTerm o1, TaggedTerm o2) {
        return collator.compare(o1.getTerm(), o2.getTerm());
      }
    });
    System.out.println("tagged: " + terms);
  }
}
