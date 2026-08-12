package com.ecotech.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ecotech.api.config.AppProperties;
import com.ecotech.api.config.CorsProperties;
import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.config.aws.AwsS3Properties;
import com.ecotech.api.config.aws.AwsSesProperties;

@SpringBootApplication
@EnableConfigurationProperties({
	JwtProperties.class,
	AwsS3Properties.class,
	AwsSesProperties.class,
	AppProperties.class,
	CorsProperties.class
})
public class EcotechApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcotechApiApplication.class, args);
	}

}
