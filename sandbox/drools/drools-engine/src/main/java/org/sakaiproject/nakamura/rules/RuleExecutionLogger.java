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

import org.drools.audit.WorkingMemoryLogger;
import org.drools.audit.event.LogEvent;
import org.drools.event.KnowledgeRuntimeEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleExecutionLogger extends WorkingMemoryLogger {

  private static final Logger LOGGER = LoggerFactory.getLogger(RuleExecutionLogger.class);
  private String rulePath;
  private boolean debugRule;

  public RuleExecutionLogger(KnowledgeRuntimeEventManager ksession, String rulePath,
      boolean debugRule) {
    super(ksession);
    this.rulePath = rulePath;
    this.debugRule = debugRule;
  }

  @Override
  public void logEventCreated(LogEvent logEvent) {
    if (debugRule) {
      LOGGER.info("Rule Execution Log for {} {} ", rulePath, logEvent);
    } else {
      LOGGER.debug("Rule Execution Log for {} {} ", rulePath, logEvent);
    }
  }

}
