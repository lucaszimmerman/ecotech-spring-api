package com.ecotech.api;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ecotech.api.config.AppProperties;
import com.ecotech.api.config.CorsProperties;
import com.ecotech.api.config.FrontendProperties;
import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.config.aws.AwsS3Properties;
import com.ecotech.api.config.aws.AwsSesProperties;

@SpringBootApplication
@EnableConfigurationProperties({
	JwtProperties.class,
	AwsS3Properties.class,
	AwsSesProperties.class,
	AppProperties.class,
	CorsProperties.class,
	FrontendProperties.class
})
public class EcotechApiApplication {

	public static void main(String[] args) {
		// Datas (LocalDateTime) sao gravadas e enviadas sempre em UTC, em qualquer
		// maquina. O frontend converte para o fuso de quem esta vendo.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(EcotechApiApplication.class, args);
	}

}
