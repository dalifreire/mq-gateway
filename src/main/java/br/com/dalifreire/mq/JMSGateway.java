package br.com.dalifreire.mq;

/**
 * Gateway utilizado para a comunicacao com as filas de mensagens.
 * 
 * @author Dali Freire - dalifreire@gmail.com
 * @since 1.0
 */
public interface JMSGateway {

    /**
     * Envia a mensagem passadas como parametro recuperando a mensagem de resposta.
     * 
     * @param message Mensagem que sera postada na fila de request.
     * @return {@link String} Mensagem de retorno.
     * @throws Exception
     */
    String sendAndReceive(final String message) throws Exception;

    /**
     * 
     * @param queueName Fila onde a mensagem sera postada.
     * @param msg Mensagem a ser postada na fila.
     * @return String ID da mensagem postada na fila.
     * @throws Exception
     */
    String sendTextMessage(final String queueName, final String msg) throws Exception;
    
    /**
     * 
     * @param msg Mensagem a ser postada na fila.
     * @return String ID da mensagem postada na fila.
     * @throws Exception
     */
    String sendTextMessage(final String msg) throws Exception;

    String receiveTextMessage(final String messageID) throws Exception;
}
