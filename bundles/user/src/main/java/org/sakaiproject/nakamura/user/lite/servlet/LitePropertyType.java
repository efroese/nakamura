/**
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
package org.sakaiproject.nakamura.user.lite.servlet;

public class LitePropertyType {

  public enum Type {
    STRING(), LONG(), DOUBLE(), DATE(), BOOLEAN(), UNDEFINED()
  }

  public static final String NAME_STRING = "String";
  public static final String NAME_LONG = "Long";
  public static final String NAME_DOUBLE = "Double";
  public static final String NAME_DATE = "Date";
  public static final String NAME_BOOLEAN = "Boolean";
  public static final String NAME_UNDEFINED = "undefined";

  public static Type create(String name) {
    if (name.equals(NAME_STRING)) {
      return Type.STRING;
    } else if (name.equals(NAME_BOOLEAN)) {
      return Type.BOOLEAN;
    } else if (name.equals(NAME_LONG)) {
      return Type.LONG;
    } else if (name.equals(NAME_DOUBLE)) {
      return Type.DOUBLE;
    } else if (name.equals(NAME_DATE)) {
      return Type.DATE;
    } else {
      return Type.UNDEFINED;
    }
  }
}
