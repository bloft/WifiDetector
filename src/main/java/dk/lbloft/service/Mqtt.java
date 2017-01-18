package dk.lbloft.service;

import com.google.common.util.concurrent.AbstractIdleService;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 */
@RequiredArgsConstructor
public class Mqtt extends AbstractIdleService {
    private static Logger log = LoggerFactory.getLogger(Mqtt.class);
    private final Properties config;

    private MqttClient client;
    private MqttConnectOptions options = new MqttConnectOptions();

    @Override
    protected void startUp() throws Exception {
        MemoryPersistence persistence = new MemoryPersistence();
        String broker = config.getProperty("mqtt.broker", "tcp://localhost:1883");
        String clientId = config.getProperty("mqtt.clientId", "WifiDetector");

        log.info("Connecting to: " + broker + " with client id: " + clientId);
        client = new MqttClient(broker, clientId, persistence);

        if(config.containsKey("mqtt.username") && config.containsKey("mqtt.password")) {
            options.setUserName(config.getProperty("mqtt.username"));
            options.setUserName(config.getProperty("mqtt.password"));
        }

        client.connect(options);
    }

    @Override
    protected void shutDown() throws Exception {
        client.disconnect();
        client.close();
    }

    public void send(String topic, String content) throws MqttException {
        if(isRunning()) {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(2);
            client.publish(topic, message);
        }
    }
}
