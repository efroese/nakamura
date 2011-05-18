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
package org.sakaiproject.nakamura.events;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.sakaiproject.nakamura.api.activemq.ConnectionFactoryService;
import org.sakaiproject.nakamura.api.cluster.ClusterTrackingService;
import org.sakaiproject.nakamura.api.events.EventDeliveryConstants;
import org.sakaiproject.nakamura.api.events.EventDeliveryConstants.EventAcknowledgeMode;
import org.sakaiproject.nakamura.api.events.EventDeliveryConstants.EventDeliveryMode;
import org.sakaiproject.nakamura.api.events.EventDeliveryConstants.EventMessageMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Bridge to send OSGi events onto a JMS topic.
 */
@Component(label = "%bridge.name", description = "%bridge.description", metatype = true, immediate = true)
@Service
public class OsgiJmsBridge implements EventHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(OsgiJmsBridge.class);

  @Property(value = "*", propertyPrivate = true)
  static final String TOPICS = EventConstants.EVENT_TOPIC;

  @Property(value = "sakai.event.bridge")
  static final String CONNECTION_CLIENT_ID = "bridge.connectionClientId";

  @Property(boolValue = false, propertyPrivate = true)
  static final String SESSION_TRANSACTED = "bridge.sessionTransacted";

  @Property(intValue = Session.AUTO_ACKNOWLEDGE, propertyPrivate = true)
  static final String ACKNOWLEDGE_MODE = "bridge.acknowledgeMode";

  @Property(value = {"org/osgi/service/log/LogEntry/LOG_DEBUG", "org/osgi/service/log/LogEntry/LOG_INFO", "org/osgi/service/log/LogEntry/LOG_TRACE"})
  private static final String IGNORE_EVENT_TOPICS = "bridge.ignore.event.topics";

  private Set<String> ignoreEventTopics = new HashSet<String>();


  @Reference
  private ConnectionFactoryService connFactoryService;


  @Reference
  protected ClusterTrackingService clusterTrackingService;

  private boolean transacted;
  private String connectionClientId;
  private int acknowledgeMode;

  private long lastMessage = System.currentTimeMillis();

  private String serverId;

  /**
   * Default constructor.
   */
  public OsgiJmsBridge() {
  }

  /**
   * Testing constructor to pass in a mocked connection factory.
   *
   * @param connFactory
   *          Connection factory to use when activating.
   * @param brokerUrl
   *          Broker url to use for comparison. This has to match what is passed in
   *          through the context properties or a new connection factory will be created
   *          not using the one passed in.
   */
  protected OsgiJmsBridge(ConnectionFactoryService connFactoryService) {
    this.connFactoryService = connFactoryService;
  }

  /**
   * Called by the OSGi container to activate this component.
   *
   * @param ctx
   */
  @SuppressWarnings("rawtypes")
  protected void activate(ComponentContext ctx) {
    Dictionary props = ctx.getProperties();

    transacted = (Boolean) props.get(SESSION_TRANSACTED);
    acknowledgeMode = (Integer) props.get(ACKNOWLEDGE_MODE);
    connectionClientId = (String) props.get(CONNECTION_CLIENT_ID);
    serverId = clusterTrackingService.getCurrentServerId();

    String[] ignoreEventTopicsValues = (String[]) props.get(IGNORE_EVENT_TOPICS);
    ignoreEventTopics.clear();

    if ( ignoreEventTopicsValues != null ) {
      for ( String iet : ignoreEventTopicsValues ) {
        ignoreEventTopics.add(iet);
      }
    }

    LOGGER.info("Session Transacted: {}, Acknowledge Mode: {}, " + "Client ID: {}",
        new Object[] { transacted, acknowledgeMode, connectionClientId });
  }

  /**
   * Called by the OSGi container to deactivate this component.
   *
   * @param ctx
   */
  protected void deactivate(ComponentContext ctx) {
  }

  /**
   * {@inheritDoc}
   *
   * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
   */
  public void handleEvent(Event event) {
    LOGGER.trace("Receiving event");
    if ( ignoreEventTopics.contains(event.getTopic()) ) {
      // Ignore Log messages in jms.
      return;
    }
    Connection conn = null;

    LOGGER.debug("Processing event {}", event);
    Session clientSession = null;
    try {

      conn = connFactoryService.getDefaultPooledConnectionFactory().createConnection();
      // conn.setClientID(connectionClientId);
      // post to JMS
      // Sessions are not thread safe, so we need to create and destroy a session, for
      // sending.
      EventDeliveryMode deliveryMode = (EventDeliveryMode) event
          .getProperty(EventDeliveryConstants.DELIVERY_MODE);
      EventMessageMode messageMode = (EventMessageMode) event
          .getProperty(EventDeliveryConstants.MESSAGE_MODE);

      EventAcknowledgeMode acknowledgeModeForEvent = (EventAcknowledgeMode) event
          .getProperty(EventDeliveryConstants.ACKNOWLEDGE_MODE);

      int clientAcknowledgeMode = acknowledgeMode;
      if (acknowledgeModeForEvent != null) {
        switch (acknowledgeModeForEvent) {
        case AUTO_ACKNOWLEDGE:
          clientAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;
          break;
        case CLIENT_ACKNOWLEDGE:
          clientAcknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
          break;
        case DUPS_OK_ACKNOWLEDGE:
          clientAcknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;
          break;
        }
      }

      clientSession = conn.createSession(transacted, clientAcknowledgeMode);

      Message msg = clientSession.createMessage();

      // may need to set a delivery mode eg persistent for certain types of messages.
      // this should be specified in the OSGi event.
      if (messageMode != null) {
        switch (messageMode) {
        case PERSISTENT:
          msg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
          break;
        case NON_PERSISTENT:
        default:
          msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
          break;
        }
      } else {
        msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
      }

      Destination destination = null;
      if (deliveryMode != null) {
        switch (deliveryMode) {
        case P2P:
          destination = clientSession.createQueue(event.getTopic());
          break;
        case BROADCAST:
        default:
          destination = clientSession.createTopic(event.getTopic());
          break;
        }
      } else {
        destination = clientSession.createTopic(event.getTopic());
      }
      MessageProducer producer = clientSession.createProducer(destination);
      msg.setJMSType(event.getTopic());

      // WARNING - cleanPropertyValue will DROP the property if it doesn't know how
      // to convert it for the JMS Message. This is not that terrible considering 
      // the JMS provider would probably fail to marshall the property value anyway.
      for (String name : event.getPropertyNames()) {
        Object obj = cleanPropertyValue(event.getProperty(name));
        if (obj != null) {
          msg.setObjectProperty(name, obj);
        }
      }
      msg.setStringProperty("clusterServerId", serverId);

      LOGGER.debug("Sending Message {} to {}  ",msg, destination);
      producer.send(msg);
    } catch (JMSException e) {
      Throwable t = e.getCause();
      if ( t != null && t.getClass().getName().equals("org.apache.activemq.transport.TransportDisposedIOException") ) {
        if ( (System.currentTimeMillis() - lastMessage) > 15000L ) {
          lastMessage = System.currentTimeMillis();
          LOGGER.info("Transport disposed, probably on shutdown, use debug level logging to see more :{} ", e.getMessage());
        }
        LOGGER.debug(e.getMessage(), e);
      } else {
        LOGGER.error(e.getMessage(), e);
      }
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }
      try {
        if (clientSession != null) {
          clientSession.close();
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Clean an event property value in order to send it as a property on a JMS Message
   * Primitive types, Strings, and Lists are returned unmodified
   * Arrays are converted to Lists. Nested Lists are not converted.
   * The values in Maps are inspected and converted using cleanProperty(obj)
   */
  @SuppressWarnings("unchecked")
  protected static Object cleanPropertyValue(Object obj){
    if (obj instanceof Byte || obj instanceof Boolean || obj instanceof Character
	     || obj instanceof Number || obj instanceof String
	     || obj instanceof List) {
      return obj;
	}
	if (obj instanceof Object[]){
	  return Arrays.asList((Object[])obj);
	}
	if (obj instanceof Map){
	  Map<String,Object> oldMap = (Map<String,Object>)obj;
	  Map<String,Object> newMap = new HashMap<String,Object>();
	  for (String key: ((Map<String,Object>)obj).keySet()){
        newMap.put(key, cleanPropertyValue(oldMap.get(key)));
	  }
	  return newMap;
	}
	return null;
  }
}
