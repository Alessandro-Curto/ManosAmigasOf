package com.manosamigas.app; // O el paquete base que decidas usar

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ManosAmigasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManosAmigasApplication.class, args);
    }
}