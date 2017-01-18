package dk.lbloft;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import dk.lbloft.service.Detector;
import dk.lbloft.service.Mqtt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.LogManager;

/**
 */
public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String ... args) throws IOException {
        Properties config = new Properties();
        switch(args.length) {
            case 0:
                config.load(new FileReader("config.properties"));
                break;
            case 1:
                config.load(new FileReader(args[0]));
                break;
            default:
                System.err.println("Only 0 or 1 args are allowed");
                System.exit(1);
        }

        LogManager.getLogManager().readConfiguration();

        Mqtt mqtt = new Mqtt(config);
        Detector detector = new Detector(config, mqtt);

        Set<Service> services = new HashSet<>();
        services.add(mqtt);
        services.add(detector);
        ServiceManager manager = new ServiceManager(services);

        manager.addListener(new ServiceManager.Listener() {
                                @Override
                                public void failure(Service service) {
                                    log.error("Error in " + service, service.failureCause());
                                    System.exit(1);
                                }
                            },
                MoreExecutors.directExecutor());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    manager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
                } catch (TimeoutException timeout) {}
            }
        });

        manager.startAsync();
    }
}
