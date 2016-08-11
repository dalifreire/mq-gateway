package br.com.dalifreire.mq.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe responsavel pelo gerenciamento dos bundles.
 * 
 * @author Dali Freire - dalifreire@gmail.com
 * @since 1.0
 */
public class BundleMenssageUtils {

    private static final Logger log = LoggerFactory.getLogger(BundleMenssageUtils.class);

    private static ResourceBundle bundle = null;
    private static final String CHAVE_INDEFINIDO = "_INDEFINIDO";


    /**
     * Obtem uma mensagem simples do arquivo de mensagens
     * 
     * @param chave Chave de cadastro da mensagem
     * @return String BundleMenssageUtils cadastrada
     */
    public static String getMensagem(String chave) {

        String mensagem = null;
        try {
            mensagem = bundle.getString(chave);
        } catch (Exception e) {
            log.error("Falha ao obter mensagem do bundle!", e);
            mensagem = chave.concat(CHAVE_INDEFINIDO);
        }

        return mensagem;
    }

    /**
     * Obtem uma mensagem composta do arquivo de mensagens
     * 
     * @param chave Chave de cadastro da mensagem
     * @param argumentos Argumentos utilizados para completar a mensagem
     * @return String BundleMenssageUtils cadastrada
     */
    public static String getMensagem(String chave, Object... argumentos) {

        String mensagem = null;
        try {
            mensagem = MessageFormat.format(bundle.getString(chave), argumentos);
        } catch (Exception e) {
            log.error("Falha ao obter mensagem do bundle!", e);
            mensagem = chave.concat(CHAVE_INDEFINIDO);
        }
        return mensagem;
    }

    static {

        try {
            bundle = ResourceBundle.getBundle("br.com.dalifreire.mq.i18n.messages");
        } catch (MissingResourceException e) {
            log.error(". Error on loading default.properties bundle. ", e);
        }
    }
}
