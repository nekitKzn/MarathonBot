package com.nekitvp.marathonbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class MarathonBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarathonBotApplication.class, args);
    }

}
