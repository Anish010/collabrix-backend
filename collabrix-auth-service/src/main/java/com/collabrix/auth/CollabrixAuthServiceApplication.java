package com.collabrix.auth;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CollabrixAuthServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(CollabrixAuthServiceApplication.class);

    Dotenv dotenv = Dotenv.configure()
            .systemProperties()   // <-- important
            .ignoreIfMissing()
            .load();

	public static void main(String[] args) {
		SpringApplication.run(CollabrixAuthServiceApplication.class, args);
        logger.info("Auth Service Started Successfully!");
	}

}
