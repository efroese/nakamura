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
<<<<<<< HEAD
<<<<<<< HEAD
    System.out.println("tagged: " + terms);
=======
    System.out.println(terms);
>>>>>>> Port of the topia termextractor from Python.
=======
    System.out.println("tagged: " + terms);
>>>>>>> Add fixes from github repo (https://github.com/turian/topia.termextract)
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

	terms = tagger.process(". Stephan");
	Assert.assertEquals(new TaggedTerm(".", ".", "."), terms.get(0));
	Assert.assertEquals(new TaggedTerm("Stephan", "NNP", "Stephan"), terms.get(1));
  }

  @Test
  public void testBreakContraction(){
	List<TaggedTerm> terms = tagger.process("can't");
	Assert.assertEquals(new TaggedTerm("can", "MD", "can"), terms.get(0));
	Assert.assertEquals(new TaggedTerm("'t", "RB", "'t"), terms.get(1));
  }

  @Test
  public void testSentence(){
	List<TaggedTerm> terms = tagger.process("The fox can't jump over the fox's tail.");
	Assert.assertEquals(new TaggedTerm("The", "DT", "The"), terms.get(0));
	Assert.assertEquals(new TaggedTerm("fox", "NN", "fox"), terms.get(1));
	Assert.assertEquals(new TaggedTerm("can", "MD", "can"), terms.get(2));
	Assert.assertEquals(new TaggedTerm("'t", "RB", "'t"), terms.get(3));
	Assert.assertEquals(new TaggedTerm("jump", "VB", "jump"), terms.get(4));
	Assert.assertEquals(new TaggedTerm("over", "IN", "over"), terms.get(5));
	Assert.assertEquals(new TaggedTerm("the", "DT", "the"), terms.get(6));
	Assert.assertEquals(new TaggedTerm("fox", "NN", "fox"), terms.get(7));
	Assert.assertEquals(new TaggedTerm("'s", "POS", "'s"), terms.get(8));
	Assert.assertEquals(new TaggedTerm("tail", "NN", "tail"), terms.get(9));
	Assert.assertEquals(new TaggedTerm(".", ".", "."), terms.get(10));
  }

  @Test
  public void testNormalizePluralForms(){
	List<TaggedTerm> terms = tagger.process("examples");
	Assert.assertEquals(new TaggedTerm("examples", "NNS", "example"), terms.get(0));

	terms = tagger.process("stresses");
	Assert.assertEquals(new TaggedTerm("stresses", "NNS", "stress"), terms.get(0));

	terms = tagger.process("cherries");
	Assert.assertEquals(new TaggedTerm("cherries", "NNS", "cherry"), terms.get(0));
  }
}
