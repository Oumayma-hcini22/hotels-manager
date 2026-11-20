package com.sip.hsm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableAsync        // envoi des emails en arrière-plan
@EnableScheduling   // tâches automatiques planifiées (@Scheduled)
public class HsmApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsmApplication.class, args);
    }
}
