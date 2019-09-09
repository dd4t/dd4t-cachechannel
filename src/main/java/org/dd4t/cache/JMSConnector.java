package org.dd4t.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class JMSConnector {
    private Properties jndiProperties;
    private String factoryName;
    private String topicName;
    private boolean isMDBMode;
    private TopicConnection topicConnection = null;
    private TopicSession topicPublisherSession = null;
    private TopicPublisher topicPublisher = null;
    private TopicSubscriber topicSubscriber = null;
    private TopicSession topicSubscriberSession = null;
    private TextMessage publicationTextMessage;

    private static final Logger LOG = LoggerFactory.getLogger(JMSConnector.class);

    public JMSConnector(Properties jndiProperties, String factoryName, String topicName, boolean isMDBMode) {
        this.jndiProperties = jndiProperties;
        this.factoryName = factoryName;
        this.topicName = topicName;
        this.isMDBMode = isMDBMode;
    }

    public void connect(MessageListener messageListener, ExceptionListener exceptionListener) throws JMSException, NamingException {
        Context jndiContext = this.jndiProperties != null ? new InitialContext(this.jndiProperties) : new InitialContext();
        TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory)jndiContext.lookup(this.factoryName);
        Topic topic = (Topic)jndiContext.lookup(this.topicName);

        this.topicConnection = topicConnectionFactory.createTopicConnection();
        if (!this.isMDBMode) {
            try
            {
                this.topicConnection.setExceptionListener(exceptionListener);
            }
            catch (JMSException e)
            {
                LOG.error("setExceptionListener failed. Most likely due to container restrictions. In these environments the MDB com.tridion.cache.JMSBean must be setup instead", e);
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
                LOG.error("setMessageListener failed. Most likely due to container restrictions. In these environments the MDB com.tridion.cache.JMSBean must be setup instead", e);
            }
        }
        this.topicPublisherSession = this.topicConnection.createTopicSession(false, 1);
        this.topicPublisher = this.topicPublisherSession.createPublisher(topic);
        this.publicationTextMessage = this.topicPublisherSession.createTextMessage();
        LOG.debug("Connected to queue, with topic: {} ", topic);
    }

}
