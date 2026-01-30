package com.parctrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParcTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParcTrackApplication.class, args);
    }
}
