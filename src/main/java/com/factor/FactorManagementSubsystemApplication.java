package com.factor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FactorManagementSubsystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FactorManagementSubsystemApplication.class, args);
    }
}
