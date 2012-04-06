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
 * specific language governing permissions and limitations
 * under the License.
 */
package org.sakaiproject.nakamura.termextract.rules;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sakaiproject.nakamura.api.termextract.TaggedTerm;
import org.sakaiproject.nakamura.api.termextract.TermExtractRule;

/**
 * Verify that noun at sentence start is truly proper.
 */
public class VerifyProperNounAtSentenceStart implements TermExtractRule {
  public void process(int index, TaggedTerm taggedTerm, List<TaggedTerm> taggedTerms, Map<String, String> lexicon) {
    String tag = taggedTerm.getTag();

    boolean isNoun = Arrays.asList("NNP", "NNPS").contains(tag);
    boolean isFirstTerm = index == 0;
    boolean lastTermIsDot = false;
    TaggedTerm lastTaggedTerm = taggedTerms.get(taggedTerms.size() - 1);
    if (taggedTerms != null && taggedTerms.size() > 0 && ".".equals(lastTaggedTerm.getTerm())) {
      lastTermIsDot = true;
    }
    
    if (isNoun && (isFirstTerm || lastTermIsDot)) {
      String lowerTerm = taggedTerm.getTerm().toLowerCase();
      String lowerTag = lexicon.get(lowerTerm);
      if (Arrays.asList("NN", "NNS").contains(lowerTag)) {
        taggedTerm.setTerm(lowerTerm).setNorm(lowerTag).setTag(lowerTag);
      }
    }
  }
}
