package com.eventiq.worker.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventiqAnalyticsWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventiqAnalyticsWorkerApplication.class, args);
    }

}
