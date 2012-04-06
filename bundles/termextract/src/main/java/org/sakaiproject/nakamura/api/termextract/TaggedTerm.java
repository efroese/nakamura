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

/**
 * Value object to represent a term, it's part-of-speech tag, and the normalized form of
 * the term.
 */
public class TaggedTerm {
  // the term itself
  private String term;
  
  // the tag of the part of speech the term represents
  private String tag;
  
  // normal form (i.e. singular) of the term
  private String norm;
  
  public TaggedTerm(String term, String tag, String norm) {
    this.term = term;
    this.tag = tag;
    this.norm = norm;
  }
  
  public String getTerm() {
    return term;
  }

  public String getTag() {
    return tag;
  }

  public String getNorm() {
    return norm;
  }

  public TaggedTerm setTerm(String term) {
    this.term = term;
    return this;
  }

  public TaggedTerm setTag(String tag) {
    this.tag = tag;
    return this;
  }

  public TaggedTerm setNorm(String norm) {
    this.norm = norm;
    return this;
  }
  
  @Override
  public String toString() {
    return "('" + term + "', '" + tag + "', '" + norm + "')";
  }
}
