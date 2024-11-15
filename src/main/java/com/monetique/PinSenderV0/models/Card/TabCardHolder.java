package com.monetique.PinSenderV0.models.Card;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Banks.TabBin;
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
@Table(name = "BASCRDHL")
public class TabCardHolder {
    @Id
    @Column(name = "PORT-NUMCLT", nullable = false)
    private String clientNumber;

    @Column(name = "PORT-NOPORT", nullable = false, unique = true)
    private String cardNumber;

    @Column(name = "PORT-NOM", nullable = true)
    private String name;

    @Column(name = "PORT-RAISON-SOC")
    private String companyName;

    @Column(name = "PORT-CODE-AGENCE", nullable = true)
    private String agencyCode;

    @Column(name = "PORT-RIB", nullable = true)
    private String rib;

    @Column(name = "PORT-FINVAL", nullable = true)
    private String finalDate;

    @Column(name = "PORT-TYP-CARTE", nullable = true)
    private String cardType;

    @Column(name = "PORT-COD-PAYS")
    private String countryCode;

    @Column(name = "PORT-NUM-CIN", nullable = true)
    private String nationalId;

    @Column(name = "PORT-PINOFFSET", nullable = true)
    private String pinOffset;

    @Column(name = "PORT-GSM")
    private String gsm;

    @Column(name = "PORT-EMAIL")
    private String email;
    @Column(name = "BANK-CODE")
    private String bankCode;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "BIN-Number", referencedColumnName = "BIN-Number", nullable = true)
    private TabBin bin;


}
