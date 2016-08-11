package br.com.dalifreire.mq.impl;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueReceiver;
import com.ibm.mq.jms.MQQueueSession;
import com.ibm.msg.client.wmq.WMQConstants;

import br.com.dalifreire.mq.JMSGateway;
import br.com.dalifreire.mq.util.BundleMenssageUtils;
import br.com.dalifreire.mq.util.LoggerUtils;
import br.com.dalifreire.mq.util.MQProperties;

public class MQGatewayImpl implements JMSGateway {

    private static final Logger logger = LoggerFactory.getLogger(MQGatewayImpl.class);

    private final MQProperties mqProperties;
    private final Long totalTimeOut;
    private final String requestQueueName;
    private final String responseQueueName;
    private final String queueManagerName;
    private final Integer characterSet;
    private final Integer encoding;
    private final String hostname;
    private final String channel;
    private final Integer port;

    public MQGatewayImpl() {
        this.mqProperties = new MQProperties();
        this.totalTimeOut = Long.valueOf(this.mqProperties.value("mq.timeout.receive.message"));
        this.requestQueueName = this.mqProperties.value("mq.requestQueue.name");
        this.responseQueueName = this.mqProperties.value("mq.responseQueue.name");
        this.queueManagerName = this.mqProperties.value("mq.queueManager.name");
        this.characterSet = Integer.valueOf(this.mqProperties.value("mq.characterSet"));
        this.encoding = Integer.valueOf(this.mqProperties.value("mq.encoding"));
        this.hostname = this.mqProperties.value("mq.hostname");
        this.channel = this.mqProperties.value("mq.channel");
        this.port = Integer.valueOf(this.mqProperties.value("mq.port"));
    }

    public String sendAndReceive(String message) throws Exception {

        if (StringUtils.isEmpty(message)) {
            throw new IllegalArgumentException(BundleMenssageUtils.getMensagem("mensagem.request.null"));
        }

        try {

            LoggerUtils.logDebug(logger, "send :: Sending request message... \n\t-> {}\n", message);

            /* envia a mensagem */
            String principalID = sendTextMessage(message);

            /* recupera a resposta */
            return receiveTextMessage(principalID);

        } catch (Exception e) {
            logger.error("send :: Error sending/receiving message!", e);
            throw e;
        }
    }

    public String sendTextMessage(String queueName, String msg) throws Exception {

        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("Queue can not be null!");
        }

        if (logger.isDebugEnabled()) {

            StringBuilder sb = new StringBuilder();
            sb.append("sendTextMessage :: Setting params... \n");
            sb.append("\t- queueManagerName = '{}' \n");
            sb.append("\t- queueName = '{}' \n");
            sb.append("\t- replyToQueueName = '{}' \n");
            sb.append("\t- hostname = '{}' \n");
            sb.append("\t- port = '{}' \n");
            sb.append("\t- channel = '{}' \n");

            LoggerUtils.logDebug(logger, sb.toString(), queueManagerName, queueName, responseQueueName, hostname, port,
                channel);
        }

        QueueConnection connection = null;
        TextMessage requestMessage;
        try {

            QueueConnectionFactory qcf = new MQQueueConnectionFactory();
            ((MQQueueConnectionFactory) qcf).setIntProperty(WMQConstants.WMQ_CONNECTION_MODE,
                WMQConstants.WMQ_CM_CLIENT);
            ((MQQueueConnectionFactory) qcf).setHostName(hostname);
            ((MQQueueConnectionFactory) qcf).setPort(port);
            ((MQQueueConnectionFactory) qcf).setQueueManager(queueManagerName);
            if (StringUtils.isNotBlank(channel)) {
                ((MQQueueConnectionFactory) qcf).setChannel(channel);
            }
            // ((MQQueueConnectionFactory) qcf).setCCSID(500);

            connection = qcf.createQueueConnection(" ", " ");
            connection.start();

            // Create a session
            MQQueueSession session = (MQQueueSession) connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);

            // Get a request queue
            Queue queue = ((MQQueueSession) session).createQueue("queue://" + queueManagerName + "/" + queueName);

            // Create message sender
            QueueSender sender = session.createSender(queue);
            requestMessage = session.createTextMessage(msg);
            // m1.setIntProperty("JMS_", MQC.MQENC_NATIVE);

            // Setting reply-to queue
            Queue queueResp = ((MQQueueSession) session)
                .createQueue("queue://" + queueManagerName + "/" + responseQueueName);
            requestMessage.setJMSReplyTo(queueResp);
            LoggerUtils.logDebug(logger, "sendTextMessage :: message \n{}", requestMessage.toString());

            sender.send(requestMessage);
            LoggerUtils.logDebug(logger, "sendTextMessage :: Message Sent! ID: {} \n",
                requestMessage.getJMSMessageID());

            return requestMessage.getJMSMessageID();

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException je) {
                    je.printStackTrace();
                }
            }
        }
    }

    /**
     * Posta a mensagem passada como parametro (de forma textual) retornando o ID gerado para a
     * mensagem.
     * 
     * @param msg
     * @return String
     * @throws Exception
     */
    public String sendTextMessage(final String msg) throws Exception {
        return sendTextMessage(requestQueueName, msg);
    }

    /**
     * Recupera as mensagens da fila de reponse de acordo com o messageID passado como parametro.
     *
     * @param messageID
     * @return String
     * @throws JMSException
     */
    public String receiveTextMessage(String messageID) throws JMSException {

        if (StringUtils.isEmpty(messageID)) {
            throw new IllegalArgumentException(BundleMenssageUtils.getMensagem("mensagem.selector.null"));
        }

        if (StringUtils.isBlank(this.responseQueueName)) {
            throw new IllegalArgumentException(BundleMenssageUtils.getMensagem("mensagem.fila.response.null"));
        }

        LoggerUtils.logDebug(logger, "receiveTextMessage :: Selecting response message using: '{}'", messageID);
        String resMessage = null;

        long finishTime = System.currentTimeMillis() + totalTimeOut;
        while (StringUtils.isBlank(resMessage) && (System.currentTimeMillis() < finishTime)) {

            QueueConnection connection = null;
            TextMessage receivedMessage;
            try {

                QueueConnectionFactory qcf = new MQQueueConnectionFactory();
                ((MQQueueConnectionFactory) qcf).setIntProperty(WMQConstants.WMQ_CONNECTION_MODE,
                    WMQConstants.WMQ_CM_CLIENT);
                ((MQQueueConnectionFactory) qcf).setHostName(hostname);
                ((MQQueueConnectionFactory) qcf).setPort(port);
                ((MQQueueConnectionFactory) qcf).setQueueManager(queueManagerName);
                if (StringUtils.isNotBlank(channel)) {
                    ((MQQueueConnectionFactory) qcf).setChannel(channel);
                }
                // ((MQQueueConnectionFactory) qcf).setCCSID(500);

                connection = qcf.createQueueConnection();
                connection.start();

                // Create a session
                MQQueueSession session = (MQQueueSession) connection.createQueueSession(false,
                    Session.CLIENT_ACKNOWLEDGE);

                // Get a response queue
                Queue responseQueue = ((MQQueueSession) session)
                    .createQueue("queue://" + queueManagerName + "/" + responseQueueName);
                MQQueueReceiver receiver = (MQQueueReceiver) session.createReceiver(responseQueue);

                receivedMessage = (TextMessage) receiver.receive(totalTimeOut);
                if (receivedMessage != null) {
                    resMessage = receivedMessage.getText();
                    receivedMessage.acknowledge();
                    LoggerUtils.logDebug(logger, "receiveTextMessage :: Response = '{}' \n", resMessage);
                }

            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException je) {
                        je.printStackTrace();
                    }
                }
            }

        }

        return resMessage;
    }

}
