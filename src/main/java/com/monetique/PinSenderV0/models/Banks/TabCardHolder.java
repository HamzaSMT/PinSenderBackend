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
    @Column(name = "PORT-NUMCLT", nullable = false)
    private String clientNumber;

    @Column(name = "PORT-NOPORT", nullable = false)
    private byte[] cardNumber;

    @Column(name = "PORT-DATCREAENR")
    private String creationDate;

    @Column(name = "PORT-DATMODENR")
    private String modificationDate;

    @Column(name = "PORT-CODANNUL")
    private String cancellationCode;

    @Column(name = "PORT-NOM", nullable = false)
    private String name;

    @Column(name = "PORT-RAISON-SOC")
    private String companyName;

    @Column(name = "PORT-CODE-AGENCE", nullable = false)
    private String agencyCode;

    @Column(name = "PORT-RIB", nullable = false)
    private String rib;

    @Column(name = "PORT-DATVAL", nullable = false)
    private String validationDate;

    @Column(name = "PORT-FINVAL", nullable = false)
    private String finalDate;

    @Column(name = "PORT-TYP-CARTE", nullable = false)
    private String cardType;

    @Column(name = "PORT-COD-PAYS")
    private String countryCode;

    @Column(name = "PORT-NUM-CIN", nullable = false)
    private String nationalId;

    @Column(name = "PORT-PINOFFSET", nullable = false)
    private String pinOffset;

    @Column(name = "PORT-GSM")
    private String gsm;

    @Column(name = "PORT-EMAIL")
    private String email;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "PORT-CODE-BANQUE", referencedColumnName = "bank_code", nullable = false)
    private TabBank bank; // Reference to TabBank
}
