package com.monetique.PinSenderV0.models.Users;


import com.monetique.PinSenderV0.tracking.ApiRequestLog;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // The user to whom this session belongs

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;  // When the session started

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;  // When the session ended (nullable, ongoing sessions have no logout time)

}
