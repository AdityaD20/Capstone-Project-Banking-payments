package com.aurionpro.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class PaymentPayrollSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentPayrollSystemApplication.class, args);
	}
}
