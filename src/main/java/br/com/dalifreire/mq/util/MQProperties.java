package br.com.dalifreire.mq.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dali Freire - dalifreire@gmail.com
 * @since 1.0
 */
public class MQProperties {

    private static final Logger log = LoggerFactory.getLogger(MQProperties.class);
    private final Properties properties;

    public MQProperties() {
        this.properties = MQProperties.properties();
    }

    public String value(String key) {
        return (String) this.properties.getProperty(key);
    }

    private static Properties properties() {
        try {

            InputStream is = ClassLoader.getSystemResourceAsStream("br/com/dalifreire/mq/application.properties");
            Properties properties = new Properties();
            properties.load(is);
            is.close();
            return properties;

        } catch (IOException e) {
            LoggerUtils.logError(log, "Falha ao carregar o arquivo de propriedades.", e);
            return null;
        }
    }

}
