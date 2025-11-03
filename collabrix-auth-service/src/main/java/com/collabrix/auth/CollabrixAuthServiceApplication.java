package com.collabrix.auth;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.collabrix")
public class CollabrixAuthServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(CollabrixAuthServiceApplication.class);

    static {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
	public static void main(String[] args) {
		SpringApplication.run(CollabrixAuthServiceApplication.class, args);
        logger.info("Auth Service Started Successfully!");
	}

}


