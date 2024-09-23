package com.monetique.PinSenderV0.models.Banks;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "CODE-BANQUE", nullable = false)
    private String bankCode;

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
}