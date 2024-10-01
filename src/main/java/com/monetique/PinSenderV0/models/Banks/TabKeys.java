package com.monetique.PinSenderV0.models.Banks;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODKEYS")
public class TabKeys {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Id
    @Column(name = "CODE-CLE", nullable = false)
    private String keyCode;

    @Id
    @Column(name = "INDEX-CLE", nullable = false)
    private String keyIndex;

    @Column(name = "DATA-CLE")
    private byte[] keyData;

    @Column(name = "CODE-SYSTEME", nullable = false)
    private String systemCode;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    private TabBank bank; // Reference to TabBank
}