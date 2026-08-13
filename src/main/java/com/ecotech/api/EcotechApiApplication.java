package com.ecotech.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ecotech.api.config.AppProperties;
import com.ecotech.api.config.AppStorageProperties;
import com.ecotech.api.config.CorsProperties;
import com.ecotech.api.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({
	JwtProperties.class,
	AppProperties.class,
	AppStorageProperties.class,
	CorsProperties.class
})
public class EcotechApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcotechApiApplication.class, args);
	}

}
