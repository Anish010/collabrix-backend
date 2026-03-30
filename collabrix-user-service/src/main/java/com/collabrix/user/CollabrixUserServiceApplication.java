package com.collabrix.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main Spring Boot Application for Collabrix User Service
 *
 * This service is responsible for:
 * - Managing user profiles
 * - Consuming Kafka events from auth-service
 * - Providing user profile APIs
 */
@SpringBootApplication
@EnableKafka
@EnableJpaAuditing
public class CollabrixUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollabrixUserServiceApplication.class, args);
        System.out.println("""
                
                ╔═══════════════════════════════════════════════════════════╗
                ║                                                           ║
                ║     🚀 Collabrix User Service Started Successfully! 🚀   ║
                ║                                                           ║
                ║     📊 Service: User Profile Management                  ║
                ║     🔌 Port: 8081                                        ║
                ║     📡 Kafka Consumer: ACTIVE                            ║
                ║     🗄️  Database: PostgreSQL (collabrix_users)           ║
                ║                                                           ║
                ║     📖 API Docs: http://localhost:8081/swagger-ui.html   ║
                ║     ❤️  Health: http://localhost:8081/actuator/health    ║
                ║                                                           ║
                ╚═══════════════════════════════════════════════════════════╝
                """);
    }
}