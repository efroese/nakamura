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
package org.sakaiproject.nakamura.termextract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
<<<<<<< HEAD
<<<<<<< HEAD
import java.util.List;
import java.util.Map;
=======
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
>>>>>>> Port of the topia termextractor from Python.
=======
import java.util.List;
import java.util.Map;
>>>>>>> Add fixes from github repo (https://github.com/turian/topia.termextract)
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.nakamura.api.termextract.TaggedTerm;
import org.sakaiproject.nakamura.api.termextract.Tagger;
import org.sakaiproject.nakamura.api.termextract.TermExtractRule;
import org.sakaiproject.nakamura.termextract.rules.CorrectDefaultNounTag;
import org.sakaiproject.nakamura.termextract.rules.DetermineVerbAfterModal;
import org.sakaiproject.nakamura.termextract.rules.NormalizePluralForms;
import org.sakaiproject.nakamura.termextract.rules.VerifyProperNounAtSentenceStart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <code>Tagger</code> interface.
 * 
 * This class is wired up as an OSGi service in serviceComponent.xml but is not dependent
 * directly on OSGi. If this class is used outside of OSGi, be sure to call
 * <code>activate()</code> after instantiating.
 */
public class TaggerImpl implements Tagger {
  private static final Logger LOGGER = LoggerFactory.getLogger(TaggerImpl.class);
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> Add fixes from github repo (https://github.com/turian/topia.termextract)
  // the original term spec
  // ([^a-zA-Z]*)([a-zA-Z-\.]*[a-zA-Z])([^a-zA-Z]*[a-zA-Z]*)
  private static final Pattern TERM_SPEC = Pattern.compile("([-\\w]*)(['\\w]*)[;]*([,\\.]?)");
  // change original term spec to use character classes
  //  private static final Pattern TERM_SPEC = Pattern.compile("([\\W\\d_]*)(([^\\W\\d_]*[-\\.]*)*[^\\W\\d_])([\\W\\d_]*[^\\W\\d_]*)");
  // add some fixes to the term spec
<<<<<<< HEAD
  private static final Pattern TERM_SPEC = Pattern.compile("([\\W\\d_]*)(([^\\W\\d_]?[-\\.]?)*[^\\W\\d_])([\\W\\d_]*[^\\W\\d_]*)");
<<<<<<< HEAD
=======
  private static final Pattern TERM_SPEC = Pattern
      .compile("([^a-zA-Z]*)([a-zA-Z-\\.]*[a-zA-Z])([^a-zA-Z]*[a-zA-Z]*)");
>>>>>>> Port of the topia termextractor from Python.
=======
>>>>>>> Add fixes from github repo (https://github.com/turian/topia.termextract)
=======
  // private static final Pattern TERM_SPEC = Pattern.compile("([\\W]*)(([^\\w_]+[-]?)*[^\\W\\d_])([\\W\\d_]*[^\\W\\d_]*)");
>>>>>>> Use a simpler regex to split terms.

  private TermExtractRule[] rules;
  private Map<String, String> tagsByTerm;

  public TaggerImpl() {
    tagsByTerm = new HashMap<String, String>();

    rules = new TermExtractRule[] { new CorrectDefaultNounTag(),
        new VerifyProperNounAtSentenceStart(), new DetermineVerbAfterModal(),
        new NormalizePluralForms() };
  }

  protected void activate() {
    InputStream lexicon = getClass().getResourceAsStream("/english-lexicon.txt");
    if (lexicon == null) {
      throw new RuntimeException(
          "Unable to find lexicon file [english-lexicon.txt] as a bundle resource.");
    }

    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(lexicon));
      String strLine = null;

      // Read File Line By Line
      while ((strLine = br.readLine()) != null) {
<<<<<<< HEAD
<<<<<<< HEAD
        String[] termAndTag = StringUtils.split(strLine, " ", 3); // split on " " since we control the file
=======
        String[] termAndTag = StringUtils.split(strLine, " ", 3);
>>>>>>> Port of the topia termextractor from Python.
=======
        String[] termAndTag = StringUtils.split(strLine, " ", 3); // split on " " since we control the file
>>>>>>> Add fixes from github repo (https://github.com/turian/topia.termextract)
        if (termAndTag.length >= 2) {
          tagsByTerm.put(termAndTag[0], termAndTag[1]);
        } else {
          LOGGER.warn("Found less than 2 parts on a line; ignoring [{}]", strLine);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      if (br != null) {
        try {
          // Close the input stream
          br.close();
        } catch (IOException e) {
          // ignore this
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.termextract.Tagger#tokenize(java.lang.String)
   */
<<<<<<< HEAD
<<<<<<< HEAD
  public List<String> tokenize(String text) {
    ArrayList<String> terms = new ArrayList<String>();
    for (String term : text.split("\\s")) {
=======
  public Set<String> tokenize(String text) {
    HashSet<String> terms = new HashSet<String>();
    for (String term : StringUtils.split(text)) {
>>>>>>> Port of the topia termextractor from Python.
=======
  public List<String> tokenize(String text) {
    ArrayList<String> terms = new ArrayList<String>();
    for (String term : text.split("\\s")) {
>>>>>>> Add fixes from github repo (https://github.com/turian/topia.termextract)
      // If the term is empty, skip it, since we probably just have
      // multiple whitespace characters.
      term = StringUtils.trimToNull(term);
      if (term == null) {
        continue;
      }
      // Now, a word can be preceded or succeeded by symbols, so let's
      // split those out
      Matcher match = TERM_SPEC.matcher(term);
      if (!match.matches()) {
        terms.add(term);
        continue;
      }
      for (int i = 1; i <= match.groupCount(); i++) {
        String subTerm = match.group(i);
        if (StringUtils.trimToNull(subTerm) != null) {
          terms.add(subTerm);
        }
      }
    }
    return terms;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.termextract.Tagger#tag(java.util.List)
   */
  public List<TaggedTerm> tag(Collection<String> terms) {
    List<TaggedTerm> taggedTerms = new ArrayList<TaggedTerm>();
    // Phase 1: Assign the tag from the lexicon. If the term is not found,
    // it is assumed to be a default noun (NND).
    for (String term : terms) {
      if (StringUtils.isBlank(term)) {
        continue;
      }

      String tag = "NND";
      if (tagsByTerm.containsKey(term)) {
        tag = tagsByTerm.get(term);
      }
      taggedTerms.add(new TaggedTerm(term, tag, term));
    }
    // Phase 2: Run through some rules to improve the term tagging and
    // normalized form.
    for (int i = 0; i < taggedTerms.size(); i++) {
      for (TermExtractRule rule : rules) {
        rule.process(i, taggedTerms.get(i), taggedTerms, tagsByTerm);
      }
    }
    return taggedTerms;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.sakaiproject.nakamura.api.termextract.Tagger#process(java.lang.String)
   */
  public List<TaggedTerm> process(String text) {
<<<<<<< HEAD
<<<<<<< HEAD
    List<String> terms = tokenize(text);
=======
    Set<String> terms = tokenize(text);
>>>>>>> Port of the topia termextractor from Python.
=======
    List<String> terms = tokenize(text);
>>>>>>> Add fixes from github repo (https://github.com/turian/topia.termextract)
    List<TaggedTerm> tags = tag(terms);
    return tags;
  }

  public String toString() {
    return "<" + getClass().getName() + " for english>";
  }
}
