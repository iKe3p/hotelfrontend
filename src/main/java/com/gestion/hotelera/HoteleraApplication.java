package com.gestion.hotelera;

import com.gestion.hotelera.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableScheduling
public class HoteleraApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoteleraApplication.class, args);
    }
}
