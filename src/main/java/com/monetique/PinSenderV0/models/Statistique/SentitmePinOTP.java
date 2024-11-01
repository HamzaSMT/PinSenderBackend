package com.monetique.PinSenderV0.models.Statistique;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;




@Entity
@Table(name = "sent_itme_pin_otp")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SentitmePinOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "bank_id", nullable = true)
    private Long bankId;

    @Column(name = "type", nullable = true)
    private String type; // OTP or PIN

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "is_counted", nullable = false)
    private boolean counted = false;
    // Constructors, Getters, Setters
}
