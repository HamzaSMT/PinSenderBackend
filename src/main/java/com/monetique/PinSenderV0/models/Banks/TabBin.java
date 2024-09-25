package com.monetique.PinSenderV0.models.Banks;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "TABBIN")
public class TabBin {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "BIN-Number", nullable = false)
    private String bin;

    @Column(name = "CODE-BANQUE")
    private String bankCode;

    @Column(name = "CODE-SYSTEME")
    private String systemCode;

    @Column(name = "TYPE-CARTE", nullable = false)
    private String cardType;

    @Column(name = "SRV-CODE")
    private String serviceCode;
}