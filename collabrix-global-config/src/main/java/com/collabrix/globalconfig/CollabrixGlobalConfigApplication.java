package com.collabrix.globalconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class CollabrixGlobalConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollabrixGlobalConfigApplication.class, args);
	}

}
