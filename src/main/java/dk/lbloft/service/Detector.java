package dk.lbloft.service;

import com.google.common.base.Splitter;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import dk.lbloft.util.Replacer;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

/**
 */
@RequiredArgsConstructor
public class Detector extends AbstractExecutionThreadService {
    private static Logger log = LoggerFactory.getLogger(Detector.class);
    private final Properties config;
    private final Mqtt mqtt;

    private BufferedReader stdout;
    private Process process;

    private void send(String address) throws MqttException {
        log.debug("New detection from: " + address);
        // ToDo: Add some kind of throttling
        String topic = "/detection/wifi/" + address;
        mqtt.send(topic, Long.toString(System.currentTimeMillis() / 1000));
    }

    @Override
    protected void startUp() throws Exception {
        String cmd = Replacer.on().replace(config).build(config.getProperty("command"));
        log.info("Running command: " + cmd);
        ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
        process = pb.start();
        stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    @Override
    protected void run() throws Exception {
        String line;
        while(isRunning() && (line = stdout.readLine()) != null) {
            List<String> data =  Splitter.on(",").limit(4).trimResults().splitToList(line);
            String mac = data.get(0);
            if(!mac.isEmpty()) {
                send(mac);
            }
        }
        if(isRunning()) {
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            stderr.lines().forEach(log::error);
            stderr.close();
            throw new Exception("Eof reached, this indicates that the process has died");
        }
    }

    @Override
    protected void shutDown() throws Exception {
        stdout.close();
        process.destroy();
    }
}
