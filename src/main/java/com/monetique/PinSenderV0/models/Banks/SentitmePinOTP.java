package com.monetique.PinSenderV0.models.Banks;

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

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "bank_id", nullable = false)
    private Long bankId;

    @Column(name = "type", nullable = false)
    private String type; // OTP or PIN

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    // Constructors, Getters, Setters
}
