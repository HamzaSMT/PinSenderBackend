package com.monetique.PinSenderV0.models.Banks;

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

    private int updatedCount;  // Number of cards updated

    @ElementCollection
    private List<String> errorCardNumbers = new ArrayList<>();  // List of card numbers that encountered errors

    private String errorDetails;  // Details of errors if any

    public CardHolderLoadReport() {
        this.loadDate = LocalDateTime.now();  // Automatically set the load date
    }


    public CardHolderLoadReport(String fileName, int createdCount, int updatedCount, List<String> errorCardNumbers, String errorDetails) {
        this.fileName = fileName;
        this.loadDate = LocalDateTime.now();
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.errorCardNumbers = errorCardNumbers;
        this.errorDetails = errorDetails;
    }


}
