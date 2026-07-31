package com.ecotech.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ecotech.api.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class EcotechApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcotechApiApplication.class, args);
	}

}
