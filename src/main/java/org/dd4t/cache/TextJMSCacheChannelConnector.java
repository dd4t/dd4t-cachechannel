package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tridion.cache.CacheChannelEventListener;
import com.tridion.cache.CacheEvent;
import com.tridion.cache.JMSCacheChannelConnector;
import com.tridion.configuration.Configuration;
import com.tridion.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

public class TextJMSCacheChannelConnector extends JMSCacheChannelConnector {

    private static final Logger LOG = LoggerFactory.getLogger(TextJMSCacheChannelConnector.class);

    @Override
    public void configure(Configuration configuration) throws ConfigurationException {
        LOG.info("Loading TextJMSCacheChannelConnector");
        Properties jndiContextProperties = null;
        if (configuration.hasChild("JndiContext"))
        {
            Configuration jndiConfig = configuration.getChild("JndiContext");
            jndiContextProperties = new Properties();
            List<Configuration> configs = jndiConfig.getChildrenByName("Property");
            for (Configuration config : configs)
            {
                String propertyKey = config.getAttribute("Name");
                String propertyValue = config.getAttribute("Value");
                jndiContextProperties.setProperty(propertyKey, propertyValue);
                LOG.debug("JMS Connector JNDI Property '{}' set with value '{}'",propertyKey,propertyValue);
            }
        }
        String topicName = configuration.getAttribute("Topic", "TridionCacheChannel");
        String topicConnectionFactoryName = configuration.getAttribute("TopicConnectionFactory", "TopicConnectionFactory");

        LOG.debug("JMS Connector TopicConnectionFactory name is {}. Topic is: {}",topicConnectionFactoryName, topicName);

        String strategy = configuration.getAttribute("Strategy", "AsyncJMS11");

        LOG.debug("JMS strategy is: {} ", strategy);

        boolean useActiveMQRest = Boolean.valueOf(configuration.getAttribute("UseActiveMQRest", "false"));
        boolean useActiveMQRestAsync = Boolean.valueOf(configuration.getAttribute("ActiveMQRestAsync", "false"));

        JMSClient client;
        if (("AsyncJMS11".equals(strategy)) || ("AsyncJMS11MDB".equals(strategy))) {
            client = new TextJMS11Approach(jndiContextProperties, topicConnectionFactoryName, topicName, "AsyncJMS11MDB".equals(strategy), useActiveMQRest, useActiveMQRestAsync);
        } else if ("SyncJMS11".equals(strategy)) {
            client = new TextSynchronousJMS11Approach(jndiContextProperties, topicConnectionFactoryName, topicName, useActiveMQRest, useActiveMQRestAsync);
        } else if (("AsyncJMS10".equals(strategy)) || ("AsyncJMS10MDB".equals(strategy))) {
            client = new TextJMS10Approach(jndiContextProperties, topicConnectionFactoryName, topicName, "AsyncJMS10MDB".equals(strategy), useActiveMQRest, useActiveMQRestAsync);
        } else {
            throw new ConfigurationException("Unknown 'Strategy':" + strategy + " for the JMS Connector");
        }

        try {
            Field clientField = this.getClass().getSuperclass().getDeclaredField("client");
            clientField.setAccessible(true);
            clientField.set(this, client);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error("Unable to set client...", e);
            throw new ConfigurationException("Unknown client for the JMS Connector");
        }
    }

    public class TextJMS11Approach extends JMSCacheChannelConnector.JMS11Approach {
        private TopicConnection topicConnection = null;
        private TopicSession topicPublisherSession = null;
        private TopicPublisher topicPublisher = null;
        private TopicSubscriber topicSubscriber = null;
        private TopicSession topicSubscriberSession = null;

        protected TextMessage publicationTextMessage;

        public TextJMS11Approach(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode, boolean useActiveMQRest, boolean useActiveMQRestAsync) {
            super(jndiProperties, factoryName, topicName, isMDBMode, useActiveMQRest, useActiveMQRestAsync);
        }

        @Override
        public void connect(MessageListener messageListener, ExceptionListener exceptionListener) throws JMSException, NamingException {
            Properties jndiProperties = this.getJndiProperties();
            Context jndiContext = jndiProperties != null ? new InitialContext(jndiProperties) : new InitialContext();
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory)jndiContext.lookup(this.getTopicConnectionFactoryName());
            Topic topic = (Topic)jndiContext.lookup(this.getTopicName());

            this.topicConnection = topicConnectionFactory.createTopicConnection();
            if (!this.isMDBMode()) {
                try
                {
                    this.topicConnection.setExceptionListener(exceptionListener);
                }
                catch (JMSException e)
                {
                    TextJMSCacheChannelConnector.LOG.error("setExceptionListener failed. Most likely due to container restrictions. In these environments the MDB com.tridion.cache.JMSBean must be setup instead", e);
                }
            }
            this.topicConnection.start();
            if (!this.isMDBMode()) {
                try
                {
                    this.topicSubscriberSession = this.topicConnection.createTopicSession(false, 1);
                    this.topicSubscriber = this.topicSubscriberSession.createSubscriber(topic, null, true);
                    this.topicSubscriber.setMessageListener(messageListener);
                }
                catch (JMSException e)
                {
                    TextJMSCacheChannelConnector.LOG.error("setMessageListener failed. Most likely due to container restrictions. In these environments the MDB com.tridion.cache.JMSBean must be setup instead", e);
                }
            }
            this.topicPublisherSession = this.topicConnection.createTopicSession(false, 1);
            this.topicPublisher = this.topicPublisherSession.createPublisher(topic);
            this.publicationTextMessage = this.topicPublisherSession.createTextMessage();
            LOG.debug("Connected to queue, with topic: {} ", topic);
        }

        @Override
        public void broadcastEvent(CacheEvent event) throws JMSException {
            try {
                String serialized = CacheEventSerializerService.serialize(event);
                this.publicationTextMessage.setText(serialized);
                this.topicPublisher.publish(this.publicationTextMessage);
                LOG.debug("Published event: {}", serialized);
            } catch (JsonProcessingException e) {
                LOG.error("Cannot serialize cache event into JSON", e);
            }
        }
    }

    public class TextJMS10Approach extends  JMSCacheChannelConnector.JMS10Approach {
        public TextJMS10Approach(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode, boolean useActiveMQRest, boolean useActiveMQRestAsync) {
            super(jndiProperties, factoryName, topicName, isMDBMode, useActiveMQRest, useActiveMQRestAsync);
        }
    }
    public class TextSynchronousJMS11Approach extends  JMSCacheChannelConnector.SynchronousJMS11Approach {
        public TextSynchronousJMS11Approach (Properties jndiProperties, String factoryName, String topicName, boolean useActiveMQRest, boolean useActiveMQRestAsync) {
            super(jndiProperties, factoryName, topicName, useActiveMQRest, useActiveMQRestAsync);
        }
    }

    @Override
    protected void handleJmsMessage(Message msg) {
        if (msg instanceof TextMessage) {
            try {
                String msgAsString = ((TextMessage) msg).getText();
                LOG.debug("processing message " + msgAsString);

                CacheEvent cacheEvent;
                try {
                    cacheEvent = CacheEventSerializerService.deserialize(msgAsString);
                } catch (IOException e) {
                    LOG.warn("error reading message", e);
                    return;
                }

                if (cacheEvent == null) {
                    LOG.warn("Ignoring unexpected message payload received on topic when calling activeMQ");
                    return;
                }

                try {
                    Field isClosed = this.getClass().getSuperclass().getDeclaredField("isClosed");
                    isClosed.setAccessible(true);
                    if (!isClosed.getBoolean(this)) {
                        Field listenerField = this.getClass().getSuperclass().getDeclaredField("listener");
                        listenerField.setAccessible(true);
                        CacheChannelEventListener listener = (CacheChannelEventListener) listenerField.get(this);
                        listener.handleRemoteEvent(cacheEvent);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    LOG.warn("error handling message", e);
                }

            } catch (JMSException jmsException) {
                LOG.error("JMS Exception occurred during reception of event. Attempting setting up JMS connectivity again", jmsException);
                try {
                    Field isValid = this.getClass().getSuperclass().getDeclaredField("isValid");
                    isValid.setAccessible(true);
                    isValid.setBoolean(this, false);

                    Method fireDisconnectMethod = this.getClass().getSuperclass().getMethod("fireDisconnect");
                    fireDisconnectMethod.setAccessible(true);
                    fireDisconnectMethod.invoke(this);

                } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
                    LOG.warn("error while trying to handle the previous exception, because of some reflection issue", e);
                }
            }
        } else {
            super.handleJmsMessage(msg);
        }
    }
}
