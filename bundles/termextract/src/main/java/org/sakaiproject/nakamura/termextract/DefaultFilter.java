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

public class DefaultFilter {

  private static final int DEFAULT_MIN_LENGTH = 1;
  private static final int DEFAULT_MAX_LENGTH = Integer.MAX_VALUE;
  private static final int DEFAULT_MIN_STRENGTH = 2;
  private static final int DEFAULT_MAX_STRENGTH = Integer.MAX_VALUE;
  private static final int DEFAULT_SINGLE_STRENGTH_MIN_OCCUR = 2;

  private int singleStrengthMinOccur;
  private int minStrength;
  private int maxStrength;
  private int minLength;
  private int maxLength;

  public DefaultFilter() {
    this(DEFAULT_SINGLE_STRENGTH_MIN_OCCUR,
        DEFAULT_MIN_STRENGTH, DEFAULT_MAX_STRENGTH,
        DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH);
  }

  public DefaultFilter(int singleStrengthMinOccur,
      int minStrength, int maxStrength,
      int minLength, int maxLength) {
    this.singleStrengthMinOccur = singleStrengthMinOccur;
    this.minStrength = minStrength;
    this.maxStrength = maxStrength;
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  public boolean filter(String word, int occur, int strength) {

    boolean keep = true;
    if (word.length() < minLength || word.length() > maxLength){
      keep = false;
    }
    else if (strength < minStrength || strength > maxStrength){
      keep = false;
    }
    else if ((strength == 1 && occur >= singleStrengthMinOccur)){
      keep = true;
    }
    return keep;
  }
}