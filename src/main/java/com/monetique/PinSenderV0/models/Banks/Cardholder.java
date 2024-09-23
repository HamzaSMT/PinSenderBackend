package com.monetique.PinSenderV0.models.Banks;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import jakarta.persistence.*;

@Entity
@Table(name = "cardholders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cardholder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    @Column(name = "cin", nullable = false)
    private String cin;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "pin", nullable = false)
    private String pin;

    @Column(name = "expiration_date", nullable = false)
    private String expirationDate;
}

