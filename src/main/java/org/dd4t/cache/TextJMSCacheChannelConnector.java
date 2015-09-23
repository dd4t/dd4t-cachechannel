package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.List;
import java.util.Properties;

public class TextJMSCacheChannelConnector extends JMSCacheChannelConnector {
    private static Logger log = LoggerFactory.getLogger(TextJMSCacheChannelConnector.class);
    public void configure(Configuration configuration)
            throws ConfigurationException {
        log.debug("loading TextJMSCacheChannelConnector");
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
                log.debug("JMS Connector JNDI Property '" + propertyKey + "' set with value '" + propertyValue + "'");
            }
        }
        String topicName = configuration.getAttribute("Topic", "TridionCacheChannel");
        String topicConnectionFactoryName = configuration.getAttribute("TopicConnectionFactory", "TopicConnectionFactory");
        log.debug("JMS Connector TopicConnectionFactory name is " + topicConnectionFactoryName + " topic is " + topicName);

        String strategy = configuration.getAttribute("Strategy", "AsyncJMS11");
        log.debug("JMS strategy is " + strategy);
        if (("AsyncJMS11".equals(strategy)) || ("AsyncJMS11MDB".equals(strategy))) {
            this.client = new TextJMS11Approach(jndiContextProperties, topicConnectionFactoryName, topicName, "AsyncJMS11MDB".equals(strategy));
        } else if ("SyncJMS11".equals(strategy)) {
            this.client = new SynchronousJMS11Approach(jndiContextProperties, topicConnectionFactoryName, topicName);
        } else if (("AsyncJMS10".equals(strategy)) || ("AsyncJMS10MDB".equals(strategy))) {
            this.client = new TextJMS10Approach(jndiContextProperties, topicConnectionFactoryName, topicName, "AsyncJMS10MDB".equals(strategy));
        } else {
            throw new ConfigurationException("Unknown 'Strategy':" + strategy + " for the JMS Connector");
        }
    }

    public class TextJMS11Approach extends JMSCacheChannelConnector.JMS11Approach {
        private TopicConnection topicConnection = null;
        private TopicSession topicPublisherSession = null;
        private TopicPublisher topicPublisher = null;
        private TopicSubscriber topicSubscriber = null;
        private TopicSession topicSubscriberSession = null;

        protected TextMessage publicationTextMessage;

        public TextJMS11Approach(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode) {
            super(jndiProperties, factoryName, topicName, isMDBMode);
        }

        public void connect(MessageListener messageListener, ExceptionListener exceptionListener)
                throws JMSException, NamingException {
            Context jndiContext = this.jndiProperties != null ? new InitialContext(this.jndiProperties) : new InitialContext();
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory)jndiContext.lookup(this.topicConnectionFactoryName);
            Topic topic = (Topic)jndiContext.lookup(this.topicName);

            this.topicConnection = topicConnectionFactory.createTopicConnection();
            if (!this.isMDBMode) {
                try
                {
                    this.topicConnection.setExceptionListener(exceptionListener);
                }
                catch (JMSException e)
                {
                    TextJMSCacheChannelConnector.log.warn("setExceptionListener failed. Most likely due to container restrictions. In these environments the MDB com.tridion.cache.JMSBean must be setup instead", e);
                }
            }
            this.topicConnection.start();
            if (!this.isMDBMode) {
                try
                {
                    this.topicSubscriberSession = this.topicConnection.createTopicSession(false, 1);
                    this.topicSubscriber = this.topicSubscriberSession.createSubscriber(topic, null, true);
                    this.topicSubscriber.setMessageListener(messageListener);
                }
                catch (JMSException e)
                {
                    TextJMSCacheChannelConnector.log.warn("setMessageListener failed. Most likely due to container restrictions. In these environments the MDB com.tridion.cache.JMSBean must be setup instead", e);
                }
            }
            this.topicPublisherSession = this.topicConnection.createTopicSession(false, 1);
            this.topicPublisher = this.topicPublisherSession.createPublisher(topic);
            this.publicationTextMessage = this.topicPublisherSession.createTextMessage();
            log.debug("Connected to queue; " + topic);
        }

        public void broadcastEvent(CacheEvent event)
                throws JMSException {
            try {
                String s = CacheEventSerializer.serialize(event);
                this.publicationTextMessage.setText(s);
                this.topicPublisher.publish(this.publicationTextMessage);
                log.debug("Published event: " + s);
            } catch (JsonProcessingException e) {
                log.error("Cannot serialize cache event into JSON", e);
            }
        }
    }

    public class TextJMS10Approach extends  JMSCacheChannelConnector.JMS10Approach {
        public TextJMS10Approach(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode) {
            super(jndiProperties, factoryName, topicName, isMDBMode);
        }
    }
    public class TextSynchronousJMS11Approach extends  JMSCacheChannelConnector.SynchronousJMS11Approach {
        public TextSynchronousJMS11Approach (Properties jndiProperties, String factoryName, String topicName) {
            super(jndiProperties, factoryName, topicName);
        }
    }
}
