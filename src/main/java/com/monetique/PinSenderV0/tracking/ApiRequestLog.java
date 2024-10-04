package com.monetique.PinSenderV0.tracking;



import com.monetique.PinSenderV0.models.Users.UserSession;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data

@Entity
@Table(name = "api_request_logs")
public class ApiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private long sessionId;  // Store the session ID

    @Column(name = "request_path")
    private String requestPath;

    @Enumerated(EnumType.STRING)  // Store the enum as a String in the database
    @Column(name = "method")
    private HttpMethodEnum method;

    @Column(name = "status_code")
    private int statusCode;  // HTTP status code returned by the request

    @Column(name = "response_time_ms")
    private long responseTimeMs; // How long the request took in milliseconds

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now(); // When the request was made

    @Column(name = "username")
    private String username;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;

    @Column(name = "request_size")
    private long requestSize;

    @Column(name = "response_size")
    private long responseSize;
    @Column(name = "ip_address")
    private String ipAddress;

}