package com.monetique.PinSenderV0.models.Card;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "card_holder_load_report")
public class CardHolderLoadReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;  // The name of the file being loaded

    private LocalDateTime loadDate;  // The date when the file was loaded

    private int createdCount;  // Number of cards created

    private int updatedCount;  // Number of cards updated  errorCount


    @ElementCollection
    private List<CardHolderErrorDetail> errorDetails = new ArrayList<>();  // List of errors



    public CardHolderLoadReport() {
        this.loadDate = LocalDateTime.now();  // Automatically set the load date
    }


    public CardHolderLoadReport(String fileName, int createdCount, int updatedCount, List<CardHolderErrorDetail> errorDetails) {
        this.fileName = fileName;
        this.loadDate = LocalDateTime.now();
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.errorDetails = errorDetails;
    }


}
