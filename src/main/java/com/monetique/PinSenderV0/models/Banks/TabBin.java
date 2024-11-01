package com.monetique.PinSenderV0.models.Banks;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "TABBIN")
public class TabBin {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "BIN-Number", nullable = false,unique=true)
    private String bin;

    @Column(name = "CODE-SYSTEME")
    private String systemCode;

    @Column(name = "TYPE-CARTE", nullable = false)
    private String cardType;

    @Column(name = "SRV-CODE")
    private String serviceCode;


    private String keyDataA;
    private String keyDataB;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    private TabBank bank; // Reference to TabBank

/*
    @JsonManagedReference
    @OneToMany(mappedBy = "bin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TabCardHolder> cardHolders;*/
}