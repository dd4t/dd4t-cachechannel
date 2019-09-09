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

public class TextJMSCacheChannelConnector extends BaseTextJMSCacheChannelConnector {

    private static final Logger LOG = LoggerFactory.getLogger(TextJMSCacheChannelConnector.class);

    @Override
    protected BaseTextJMSCacheChannelConnector.TextJMS11Approach getTextJMS11Approach(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode) {
        return new BaseTextJMSCacheChannelConnector.TextJMS11Approach(jndiProperties, factoryName, topicName, isMDBMode);
    }
    @Override
    protected BaseTextJMSCacheChannelConnector.SynchronousJMS11Approach getSynchronousJMS11Approach(Properties jndiProperties, String factoryName, String topicName) {
        return new BaseTextJMSCacheChannelConnector.SynchronousJMS11Approach(jndiProperties, factoryName, topicName);
    }
    @Override
    protected BaseTextJMSCacheChannelConnector.TextJMS10Approach getTextJMS10Approach(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode) {
        return new BaseTextJMSCacheChannelConnector.TextJMS10Approach(jndiProperties, factoryName, topicName, isMDBMode);
    }

    public class TextJMS11Approach extends JMS11Approach {
        private TopicConnection topicConnection = null;
        private TopicSession topicPublisherSession = null;
        private TopicPublisher topicPublisher = null;
        private TopicSubscriber topicSubscriber = null;
        private TopicSession topicSubscriberSession = null;

        protected TextMessage publicationTextMessage;

        public TextJMS11Approach(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode) {
            super(jndiProperties, factoryName, topicName, isMDBMode);
        }

        @Override
        public void connect(MessageListener messageListener, ExceptionListener exceptionListener) throws JMSException, NamingException {
            JMSConnector jmsConnector = new JMSConnector(jndiProperties, topicConnectionFactoryName, topicName, isMDBMode);
            jmsConnector.connect(messageListener, exceptionListener);
        }
    }

    public class TextJMS10Approach extends  JMS10Approach {
        public TextJMS10Approach(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode) {
            super(jndiProperties, factoryName, topicName, isMDBMode);
        }
    }
    public class TextSynchronousJMS11Approach extends  SynchronousJMS11Approach {
        public TextSynchronousJMS11Approach (Properties jndiProperties, String factoryName, String topicName) {
            super(jndiProperties, factoryName, topicName);
        }
    }
}
