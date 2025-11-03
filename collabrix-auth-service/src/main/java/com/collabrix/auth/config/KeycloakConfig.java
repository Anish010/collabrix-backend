package com.collabrix.auth.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUser;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin-client-id:admin-cli}")
    private String adminClientId;

    @Bean
    public Keycloak keycloakAdmin() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .username(adminUser)
                .password(adminPassword)
                .clientId(adminClientId)
                .build();
    }

    @Bean
    public WebClient keycloakWebClient(@Value("${keycloak.server-url}") String kcUrl) {
        return WebClient.builder()
                .baseUrl(kcUrl)
                .build();
    }
}
