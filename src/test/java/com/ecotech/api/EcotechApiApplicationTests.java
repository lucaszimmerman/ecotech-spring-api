package com.ecotech.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.ecotech.api.support.TestJwtProperties;

@SpringBootTest
@ActiveProfiles("test")
class EcotechApiApplicationTests {

	@DynamicPropertySource
	static void registerJwtProperties(DynamicPropertyRegistry registry) {
		TestJwtProperties.register(registry);
	}

	@Test
	void contextLoads() {
	}

}
