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
package org.sakaiproject.nakamura.api.messagebucket;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A message bucket contains stuff to be transported
 */
public interface MessageBucket {

  /**
   * @return true if the bucket has content that can be distributed
   */
  boolean isReady();

  /**
   * @param waiter to be added to the list of waiters waiting
   */
  void addWaiter(Waiter waiter);

  /**
   * @param waiter to be removed from the list of waiters waiting.
   */
  void removeWaiter(Waiter waiter);

  /**
   * @param response send the contents of the bucket out over the response.
   * @throws MessageBucketException 
   */
  void send(HttpServletResponse response) throws  MessageBucketException;

  /**
   * Unbind the request that was bound to this bucket using this token.
   * @param token the token that was used to bind with.
   * @param request the request which was bound using the token
   */
  void unbind(String token, HttpServletRequest request);

  void bind(String token, HttpServletRequest request);

}
