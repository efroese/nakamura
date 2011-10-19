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
package org.sakaiproject.nakamura.rules;

import com.sample.DroolsTest.Message;

import org.sakaiproject.nakamura.api.rules.RuleContext;
import org.sakaiproject.nakamura.api.rules.RuleExecutionPreProcessor;

import java.util.HashMap;
import java.util.Map;

public class MesageRuleExcutionPreProcessor implements RuleExecutionPreProcessor {

  public Map<RulesObjectIdentifier, Object> getAdditonalGlobals(RuleContext ruleContext) {
    Map<RulesObjectIdentifier, Object> inputs = new HashMap<RulesObjectIdentifier, Object>();
    // this should be ignored and not cause a failure
    RulesObjectIdentifier invalid = new RulesObjectIdentifier("ignore-this-global", null);
    inputs.put(invalid, new Object());
    return inputs;
  }

  public Map<RulesObjectIdentifier, Object> getAdditonalInputs(RuleContext ruleContext) {
    Map<RulesObjectIdentifier, Object> inputs = new HashMap<RulesObjectIdentifier, Object>();
    RulesObjectIdentifier ihello = new RulesObjectIdentifier("message", null);
    Message message = new Message();
    message.setStatus(Message.HELLO);
    message.setMessage("Hi there");
    inputs.put(ihello, message);
    RulesObjectIdentifier igoodby = new RulesObjectIdentifier("goodbyMessage", null);
    Message goodbyMessage = new Message();
    message.setStatus(Message.GOODBYE);
    message.setMessage("Bye");
    inputs.put(igoodby, goodbyMessage);
    return inputs;
  }

}
