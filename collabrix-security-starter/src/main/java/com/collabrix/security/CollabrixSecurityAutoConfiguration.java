package com.collabrix.security;

import com.collabrix.security.config.ResourceServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Collabrix Security Starter.
 * Automatically imported by Spring Boot's auto-configuration mechanism.
 */
@Configuration
@ConditionalOnProperty(prefix = "collabrix.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(ResourceServerConfig.class)
public class CollabrixSecurityAutoConfiguration {
    // Auto-configuration class - no additional beans needed
}