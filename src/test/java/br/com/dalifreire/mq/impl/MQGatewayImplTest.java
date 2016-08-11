package br.com.dalifreire.mq.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import br.com.dalifreire.mq.JMSGateway;
import br.com.dalifreire.mq.util.MQProperties;

public class MQGatewayImplTest {

    @Test
    public void testSendAndReceive() {
        try {

            /* mensagem a ser postada */
            StringBuilder msgRequest = new StringBuilder();
            msgRequest.append("{\"msg\": \"Shoryuken!\"}");

            /* posta a mensagem na fila (request) e recupera a resposta */
            JMSGateway jmsMqGateway = new MQGatewayImpl();
            String msgResponse = jmsMqGateway.sendAndReceive(msgRequest.toString());

            /* como nao temos ninguem ouvindo a fila, a resposta sera null */
            Assert.assertTrue(StringUtils.equals(msgRequest.toString(), msgResponse) || msgResponse == null);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testSendTextMessage() {
        try {

            /* mensagem a ser postada */
            StringBuilder msgRequest = new StringBuilder();
            msgRequest.append("{\"msg\": \"Hadouken!\"}");

            /* posta a mensagem na fila */
            JMSGateway jmsMqGateway = new MQGatewayImpl();
            String messageID = jmsMqGateway.sendTextMessage(msgRequest.toString());

            Assert.assertNotNull(messageID);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testReceiveTextMessage() {
        try {

            MQProperties mqProperties = new MQProperties();
            String responseQueueName = mqProperties.value("mq.responseQueue.name");

            /* mensagem a ser postada */
            StringBuilder msgRequest = new StringBuilder();
            msgRequest.append("{\"msg\": \"Shoryuken!\"}");

            /* posta a mensagem na fila */
            JMSGateway jmsMqGateway = new MQGatewayImpl();
            String messageID = jmsMqGateway.sendTextMessage(responseQueueName, msgRequest.toString());
            Assert.assertNotNull(messageID);

            /* realiza a leitura da mensagem postada */
            String msgResponse = jmsMqGateway.receiveTextMessage(messageID);

            Assert.assertEquals(msgRequest.toString(), msgResponse);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
