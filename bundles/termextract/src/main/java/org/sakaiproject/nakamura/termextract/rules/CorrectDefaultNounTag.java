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

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.termextract.TaggedTerm;
import org.sakaiproject.nakamura.api.termextract.TermExtractRule;

/**
 * Determine whether a default noun is plural or singular.
 *
 * NOTE from erik@hallwaytech.com I can't find a term in english-lexicon.txt with the tag NND
 */
public class CorrectDefaultNounTag implements TermExtractRule {
  public void process(int index, TaggedTerm taggedTerm, List<TaggedTerm> taggedTerms, Map<String, String> lexicon) {
    if ("NND".equals(taggedTerm.getTag())) {
      String term = taggedTerm.getTerm();
      if (term.endsWith("s")) {
        taggedTerm.setTag("NNS");
        taggedTerm.setNorm(StringUtils.substring(term, 0, -1));
      } else {
        taggedTerm.setTag("NN");
      }
    }
  }
}
