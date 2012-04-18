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

import junit.framework.Assert;

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
    tagger.activate();
  }
  
  @Test
  public void testTagging() throws Exception {
    String txt = TermExtractUtil.readExampleText("/example.txt");
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

  @Test
  public void testTermExtraction() {
	  List<TaggedTerm> terms = tagger.process("Ikea");
	  Assert.assertEquals(new TaggedTerm("Ikea", "NN", "Ikea"), terms.get(0));
	  
	  terms = tagger.process("Ikeas");
	  Assert.assertEquals(new TaggedTerm("Ikeas", "NNS", "Ikea"), terms.get(0));
  }
  
  @Test
  public void testLeadingPunctuation(){
	  List<TaggedTerm> terms = tagger.process(". Police");
	  Assert.assertEquals(new TaggedTerm(".", ".", "."), terms.get(0));
	  Assert.assertEquals(new TaggedTerm("police", "NN", "police"), terms.get(1));
  }
 
  @Test
  public void testSentence(){
	  List<TaggedTerm> terms = tagger.process("The fox can't jump over the fox's tail.");
	  Assert.assertEquals(new TaggedTerm("The", "DT", "The"), terms.get(0));
	  Assert.assertEquals(new TaggedTerm("fox", "NN", "fox"), terms.get(1));
	  Assert.assertEquals(new TaggedTerm("can't", "MD", "can't"), terms.get(2));
	  Assert.assertEquals(new TaggedTerm("jump", "VB", "jump"), terms.get(3));
	  Assert.assertEquals(new TaggedTerm("over", "IN", "over"), terms.get(4));
	  Assert.assertEquals(new TaggedTerm("the", "DT", "the"), terms.get(5));
	  Assert.assertEquals(new TaggedTerm("fox's", "NNS", "fox'"), terms.get(6));
	  Assert.assertEquals(new TaggedTerm("tail", "NN", "tail"), terms.get(7));
	  Assert.assertEquals(new TaggedTerm(".", ".", "."), terms.get(8));
  }
}
