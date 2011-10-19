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
package org.sakaiproject.nakamura.api.http.qos;

import org.mortbay.util.ajax.Continuation;

/**
 * A QoSToken is a class that can be added to the request as an attribute before the QoSFilter processes the request to modify the way in which the 
 * request us handled.
 */
public interface QoSToken {
  /**
   * The name of the attribute where this object must be added to the request
   */
  public static final String CONTROL_ATTR = QoSToken.class.getName();

  /**
   * @return a lock object to prevent concurrent access to the continuation
   */
  Object getMutex();

  /**
   * Perform any necessary release operations to release any semaphores, or otherwise, that were aquired. This method should also notify other requests that are suspended waiting on resources.
   */
  void release();

  /**
   * @return the max time in ms that this request should be suspended.
   */
  long getSuspendTime();

  /**
   * Aquire a semaphore to allow this request to continue.
   * @param waitMs the time in ms to wait for the semaphore processing to complete (dont confuse with sustend time which is the total time the request may wait for)
   * @return true of a semaphore was granted
   * @throws InterruptedException
   */
  boolean acquire(long waitMs) throws InterruptedException;

  /**
   * Acquire already acquired semaphore.
   * @throws InterruptedException
   */
  void acquire() throws InterruptedException;

  /**
   * Queue a continuation pending more resources
   * @param continuation
   */
  void queue(Continuation continuation);

}
