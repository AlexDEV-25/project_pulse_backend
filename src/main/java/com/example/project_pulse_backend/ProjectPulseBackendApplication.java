package com.example.project_pulse_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ProjectPulseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectPulseBackendApplication.class, args);
    }

}
