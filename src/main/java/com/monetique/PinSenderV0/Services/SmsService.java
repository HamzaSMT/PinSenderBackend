package com.monetique.PinSenderV0.Services;


import com.monetique.PinSenderV0.security.jwt.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final WebClient webClient;

    public SmsService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> sendSms(String to, String message) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cgi-bin/sendsms")
                        .queryParam("username", "PINSENDER")
                        .queryParam("password", "PIN2024")
                        .queryParam("from", "MONETIQUE")
                        .queryParam("to", to)
                        .queryParam("text", message)
                        .build())
                .retrieve()  // Retrieve response
                .onStatus(status -> status.isError(), clientResponse -> {
                    // Handle error response
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                // Log error or handle it based on the response body
                                return Mono.error(new RuntimeException(
                                        "Failed to send SMS. Status code: " + clientResponse.statusCode() +
                                                ", Error: " + errorBody));
                            });
                })
                .bodyToMono(String.class)  // Convert the response body to String
                .doOnError(WebClientResponseException.class, ex -> {
                    // Handle WebClient-specific exceptions (like 404, 500, etc.)
                    logger.error("Error occurred while sending SMS: {}", ex.getMessage());
                })
                .onErrorResume(e -> {
                    // Handle any other errors (fallback handling)
                    logger.error("Fallback: Failed to send SMS to {}: {}", to, e.getMessage());
                    return Mono.just("SMS sending failed.");
                });
    }
}