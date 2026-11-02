package com.voiceforge;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VoiceForgeApplication {

    public static void main(String[] args) {
        // The PG JDBC driver sends the JVM zone on connect; legacy IDs such as
        // Asia/Calcutta (Windows default for IST) are rejected by newer Postgres.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(VoiceForgeApplication.class, args);
    }
}
