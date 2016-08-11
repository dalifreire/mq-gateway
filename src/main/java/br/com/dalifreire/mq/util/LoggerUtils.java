package br.com.dalifreire.mq.util;

import java.util.List;

import org.slf4j.Logger;

/**
 * Classe utilitaria para logs.
 * 
 * @author Dali Freire - dalifreire@gmail.com
 * @since 1.0
 */
public class LoggerUtils {

    /**
     * Verifica estado de logger e efetua log de nivel debug.
     * 
     * @param {@link Logger} logger
     * @param {@link String} mensagem
     * @param {@link Object} params
     */
    public static void logDebug(Logger logger, String mensagem, Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug(mensagem, params);
        }
    }

    /**
     * Verifica estado de logger e efetua log de nivel error.
     * 
     * @param {@link Logger} logger
     * @param {@link String} mensagem
     * @param {@link Object} params
     */
    public static void logError(Logger logger, String mensagem, Object... params) {
        if (logger.isErrorEnabled()) {
            logger.error(mensagem, params);
        }
    }

    /**
     * Verifica estado de logger e efetua log de nivel trace.
     * 
     * @param {@link Logger} logger
     * @param {@link String} mensagem
     * @param {@link Object} params
     */
    public static void logTrace(Logger logger, String mensagem, Object... params) {
        if (logger.isTraceEnabled()) {
            logger.trace(mensagem, params);
        }
    }

    /**
     * Efetua log de varios erros.
     * 
     * @param {@link Logger} logger
     * @param {@link List} strings
     */
    public static void logErrors(Logger logger, List<String> strings) {
        if (strings != null) {
            for (String error : strings) {
                logger.error("logErrors :: " + error);
            }
        }
    }

}
