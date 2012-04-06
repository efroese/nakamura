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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.termextract.ExtractedTerm;
import org.sakaiproject.nakamura.api.termextract.TaggedTerm;
import org.sakaiproject.nakamura.api.termextract.Tagger;
import org.sakaiproject.nakamura.api.termextract.TermExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link TermExtractor} interface.
 * 
 * This class is wired up as an OSGi service in serviceComponent.xml but is not dependent
 * directly on OSGi.
 */
public class TermExtractorImpl implements TermExtractor {
  private static final Logger LOGGER = LoggerFactory.getLogger(TermExtractorImpl.class);
private static final int SEARCH = 0;
  private static final int NOUN = 1;

  private Tagger tagger;
  private DefaultFilter filter;

  public TermExtractorImpl() {
    filter = new DefaultFilter();
  }

  public TermExtractorImpl(Tagger tagger) {
    this();
    if (tagger == null) {
      tagger = new TaggerImpl();
      ((TaggerImpl) tagger).activate();
    }
    this.tagger = tagger;
  }

  public List<ExtractedTerm> process(String text) {
    if (tagger == null) {
      LOGGER.info("Returning null because the tagger is null.");
      return null;
    }
    
    List<TaggedTerm> terms = tagger.process(text);
    return extract(terms);
  }

  public String toString() {
    return "<" + getClass().getName() + " using " + tagger + ">";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.termextract.TermExtractor#extract(java.lang.String)
   */
  @Override
  public List<ExtractedTerm> extract(List<TaggedTerm> taggedTerms) {
    Map<String, Integer> terms = new HashMap<String, Integer>();
    // Phase 1: A little state machine is used to build simple and
    // composite terms.
    Map<String, String> multiterm = new HashMap<String, String>();
    int state = SEARCH;
    while (!taggedTerms.isEmpty()) {
      TaggedTerm taggedTerm = taggedTerms.remove(0);
      String term = taggedTerm.getTerm();
      String tag = taggedTerm.getTag();
      String norm = taggedTerm.getNorm();

      if (state == SEARCH && tag.charAt(0) == 'N') {
        state = NOUN;
        add(term, norm, multiterm, terms);
      } else if (state == SEARCH && "JJ".equals(tag)
          && Character.isUpperCase(term.charAt(0))) {
        state = NOUN;
        add(term, norm, multiterm, terms);
      } else if (state == NOUN && tag.charAt(0) == 'N') {
        add(term, norm, multiterm, terms);
      } else if (state == NOUN && tag.charAt(0) != 'N') {
        state = SEARCH;
        if (multiterm.size() > 1) {
          String word = StringUtils.join(multiterm.keySet(), " ");
          increaseCount(terms, word);
        }
        multiterm = new HashMap<String, String>();
      }
    }
    // Phase 2: Only select the terms that fulfill the filter criteria.
    // Also create the term strength.
    List<ExtractedTerm> retTerms = new ArrayList<ExtractedTerm>();
    for (Entry<String, Integer> term : terms.entrySet()) {
      String word = term.getKey();
      int occurences = term.getValue();
      int strength = StringUtils.split(term.getKey()).length;
      if (filter.filter(word, state, strength)) {
        retTerms.add(new ExtractedTerm(word.trim(), occurences, strength));
      }
    }
    return retTerms;
  }

  private void add(String term, String norm, Map<String, String> multiterm,
      Map<String, Integer> terms) {
    multiterm.put(term, norm);
    increaseCount(terms, norm);
  }

  /**
   * @param terms
   * @param norm
   */
  private void increaseCount(Map<String, Integer> terms, String norm) {
    if (terms.containsKey(norm)) {
      terms.put(norm, terms.get(norm) + 1);
    } else {
      terms.put(norm, 1);
    }
  }

  protected void bindTagger(Tagger tagger) {
    this.tagger = tagger;
  }
  
  protected void unbindTagger() {
    this.tagger = null;
  }
  
  private class DefaultFilter {
    private int singleStrengthMinOccur;
    private int noLimitStrength;

    public DefaultFilter() {
      this(3, 2);
    }

    public DefaultFilter(int singleStrengthMinOccur, int noLimitStrength) {
      this.singleStrengthMinOccur = singleStrengthMinOccur;
      this.noLimitStrength = noLimitStrength;
    }

    public boolean filter(String word, int occur, int strength) {
      return ((strength == 1 && occur >= singleStrengthMinOccur) || (strength >= noLimitStrength));
    }
  }
}
