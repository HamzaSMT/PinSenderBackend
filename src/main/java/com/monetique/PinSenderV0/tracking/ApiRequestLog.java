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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private UserSession session;  // Link to the session in which this request was made

    @Column(name = "request_path", nullable = false)
    private String requestPath;

    @Enumerated(EnumType.STRING)  // Store the enum as a String in the database
    @Column(name = "method")
    private HttpMethodEnum method;

    @Column(name = "status_code", nullable = false)
    private int statusCode;  // HTTP status code returned by the request

    @Column(name = "response_time_ms", nullable = false)
    private long responseTimeMs; // How long the request took in milliseconds

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now(); // When the request was made
}