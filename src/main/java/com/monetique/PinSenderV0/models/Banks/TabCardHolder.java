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
@Table(name = "BASCRDHL")
public class TabCardHolder {
    @Id
    @Column(name = "PORT-NUMCLT", nullable = true)
    private String clientNumber;

    @Column(name = "PORT-NOPORT", nullable = true, unique = true)
    private String cardNumber;

    @Column(name = "PORT-DATCREAENR")
    private String creationDate;

    @Column(name = "PORT-DATMODENR")
    private String modificationDate;

    @Column(name = "PORT-CODANNUL")
    private String cancellationCode;

    @Column(name = "PORT-NOM", nullable = true)
    private String name;

    @Column(name = "PORT-RAISON-SOC")
    private String companyName;

    @Column(name = "PORT-CODE-AGENCE", nullable = true)
    private String agencyCode;

    @Column(name = "PORT-RIB", nullable = true)
    private String rib;

    @Column(name = "PORT-DATVAL", nullable = true)
    private String validationDate;

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

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "BIN-Number", referencedColumnName = "BIN-Number", nullable = true)
    private TabBin bin;


    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "PORT-CODE-BANQUE", referencedColumnName = "bank_code", nullable = true)
    private TabBank bank; // Reference to TabBank
}
