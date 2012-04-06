/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.nakamura.termextract.rules;

import java.util.List;
import java.util.Map;

import org.sakaiproject.nakamura.api.termextract.TaggedTerm;
import org.sakaiproject.nakamura.api.termextract.TermExtractRule;

/**
 * Determine the verb after a modal verb to avoid accidental noun detection.
 */
public class DetermineVerbAfterModal implements TermExtractRule {
  public void process(int index, TaggedTerm taggedTerm, List<TaggedTerm> taggedTerms, Map<String, String> lexicon) {
    if (!"MD".equals(taggedTerm.getTag())) {
      return;
    }
    int lenTerms = taggedTerms.size();
    index++;
    while (index < lenTerms) {
      TaggedTerm nextTaggedTerm = taggedTerms.get(index);
      if ("RB".equals(nextTaggedTerm.getTag())) {
        index++;
        continue;
      }
      if ("NN".equals(nextTaggedTerm.getTag())) {
        nextTaggedTerm.setTag("VB");
      }
      break;
    }
  }
}
