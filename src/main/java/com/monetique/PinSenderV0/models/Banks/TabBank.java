package com.monetique.PinSenderV0.models.Banks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.monetique.PinSenderV0.models.Card.TabCardHolder;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tab_bank")
public class TabBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Column(name = "bank_code", unique = true, nullable = false,length = 5)
    private String bankCode;

    @Column(name = "LIBELLE_BANQUE", length = 50)
    private String libelleBanque;

    @Column(name = "ENSEIGNE_BANQUE", length = 10)
    private String enseigneBanque;

    @Column(name = "ICA", length = 5)
    private String ica;

    @Column(name = "BIN_ACQUEREUR_VISA", length = 6)
    private String binAcquereurVisa;

    @Column(name = "BIN_ACQUEREUR_MCD", length = 6)
    private String binAcquereurMcd;

    @Column(name = "CTB", length = 3)
    private String ctb;

    @Column(name = "BANQUE_ETRANGERE")
    private boolean banqueEtrangere;

    @Transient // This field is not persisted in the database
    private byte[] logoContent;

    private String logoFilePath ;

    private String adminUsername;
    @JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TabBin> bins = new HashSet<>();


    public byte[] getLogoContent() {
        if (logoFilePath != null) {
            try {
                Path path = Paths.get(logoFilePath);
                logoContent = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new RuntimeException("Error reading logo file", e);
            }
        }
        return logoContent;
    }


}