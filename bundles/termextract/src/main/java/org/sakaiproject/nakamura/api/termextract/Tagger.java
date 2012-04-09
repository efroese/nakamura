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
package org.sakaiproject.nakamura.api.termextract;

import java.util.Collection;
import java.util.List;

/**
 * A utility to provide POS tag extractions from a given text.
 */
public interface Tagger {
  /**
   * Tokenize the given text into single words.
   * 
   * @param text
   */
  List<String> tokenize(String text);

  /**
   * Returns the tagged list of terms.
   * 
   * Additionally, all terms are normalized.
   * 
   * The output format is a list of: (term, tag, normalized-term)
   */
  List<TaggedTerm> tag(Collection<String> terms);

  /**
   * Convenience method to call <code>tag(tokenize(text))</code>.
   * 
   * @param text
   */
  List<TaggedTerm> process(String text);
  
}
