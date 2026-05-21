package com.ssafy.culturepick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CulturepickApplication {

    public static void main(String[] args) {
        SpringApplication.run(CulturepickApplication.class, args);
    }

}
