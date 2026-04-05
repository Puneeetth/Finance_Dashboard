package com.finance.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FinanceDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceDashboardApplication.class, args);
    }
}
