package de.pribluda.eth.tanktruck;

import de.pribluda.eth.tanktruck.config.ApplicationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
@EnableScheduling
@EnableAsync
public class Truck {

    public static void main(String[] args) {
        new org.springframework.boot.SpringApplication(Truck.class).run(args);
    }
}
