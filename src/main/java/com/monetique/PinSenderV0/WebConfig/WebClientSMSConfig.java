package com.monetique.PinSenderV0.WebConfig;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientSMSConfig {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("http://172.17.9.210:13002")  // Common base URL for SMS service
                .defaultHeader("Content-Type", "application/json")  // Default headers
                .build();
    }
}