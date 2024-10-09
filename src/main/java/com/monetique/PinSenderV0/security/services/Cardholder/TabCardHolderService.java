package com.monetique.PinSenderV0.security.services.Cardholder;

import com.monetique.PinSenderV0.Interfaces.ICardholderService;
import com.monetique.PinSenderV0.RabbitMQ.RabbitMQConfig;
import com.monetique.PinSenderV0.models.Banks.TabCardHolder;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.TabCardHolderresponse;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.TabBinRepository;
import com.monetique.PinSenderV0.repository.TabCardHolderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TabCardHolderService implements ICardholderService {
    @Autowired
    TabCardHolderRepository tabCardHolderRepository;
    @Autowired
    BankRepository bankRepository;
    @Autowired
    TabBinRepository tabBinRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    private static final String EXCHANGE_NAME = "cardholder.exchange";
    private static final String ROUTING_KEY = "cardholder.routingKey";;




    @Override
    public List<TabCardHolderresponse> getAllCardHolders() {
        List<TabCardHolder> cardHolders = tabCardHolderRepository.findAll();
        List<TabCardHolderresponse> response = cardHolders.stream()
                .map(TabCardHolderresponse::new)
                .collect(Collectors.toList());
        return response;
    }
    @Override
    public TabCardHolder extractCardHolderAttributes(String line) {
        TabCardHolder cardHolder = new TabCardHolder();

        // Extract attributes from specific positions in the line
        cardHolder.setClientNumber(line.substring(0, 30)); // Client number
        cardHolder.setCardNumber(line.substring(4, 21)); // Card number
        cardHolder.setName(line.substring(27, 53)); // Name (adjust positions accordingly)
        cardHolder.setNationalId(line.substring(206, 219)); // National ID
        cardHolder.setRib(line.substring(100, 120)); // RIB
        cardHolder.setGsm(line.substring(303, 317)); // GSM
        cardHolder.setEmail(line.substring(160, 180));
        cardHolder.setCompanyName(line.substring(53, 79));// Email
        cardHolder.setAgencyCode(line.substring(320, 330));


        // Add other field extractions here (e.g., bin, bank, etc.)

        return cardHolder;
    }
    @Override
    public void processCardHolderLine(String line) {
        // Step 1: Extract the card number from the line
        String cardNumber = line.substring(4, 21);  // Assuming the card number is from position 5 to 21

        System.out.println("Processing Card Holder with card number: " + cardNumber);

        // Step 2: Check if the cardholder already exists in the system
        TabCardHolder existingCardHolder = tabCardHolderRepository.findByCardNumber(cardNumber);
        System.out.println(" Card Holder : " +existingCardHolder);
        if (existingCardHolder != null) {
            // Step 3: If the cardholder exists, update their details
            System.out.println("Cardholder exists, updating details for card number: " + cardNumber);
            updateCardHolder(existingCardHolder, line);  // Pass the existing cardholder to the update method
        } else {
            // Step 4: If the cardholder doesn't exist, create a new one
            System.out.println("Cardholder doesn't exist, creating new record for card number: " + cardNumber);
            createCardHolder(line);
        }
    }
@Override
public void createCardHolder(String line) {
        TabCardHolder newCardHolder = extractCardHolderAttributes(line);  // Extract attributes from the line

        // Save the new cardholder
        tabCardHolderRepository.save(newCardHolder);
    }
    @Override
    public void updateCardHolder(TabCardHolder existingCardHolder, String line) {
        TabCardHolder updatedCardHolder = extractCardHolderAttributes(line);  // Extract updated attributes

        // Update the fields in the existing cardholder entity
        existingCardHolder.setName(updatedCardHolder.getName());
        existingCardHolder.setBin(updatedCardHolder.getBin());
        existingCardHolder.setCompanyName(updatedCardHolder.getCompanyName());
        existingCardHolder.setNationalId(updatedCardHolder.getNationalId());
        // Update other fields as needed
        try {
            tabCardHolderRepository.save(existingCardHolder);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Error updating cardholder: " + e.getMessage());
            // Handle the error (e.g., log it or rethrow it)
        }
    }
    @Override
    public String processCardHolderLines(List<String> lines) {
        int createdCount = 0;
        int updatedCount = 0;
        int errorCount = 0;
        List<String> errorLines = new ArrayList<>();

        for (String line : lines) {
            try {
                processCardHolderLine(line);
                // Count the successful operations
                TabCardHolder cardHolder = extractCardHolderAttributes(line);
                if (tabCardHolderRepository.findByCardNumber(cardHolder.getCardNumber()) != null) {
                    updatedCount++;
                } else {
                    createdCount++;
                }
            } catch (DataIntegrityViolationException e) {
                errorCount++;
                errorLines.add("Error processing line: \"" + line + "\", Message: " + e.getMessage());
                System.err.println("Error processing line: " + e.getMessage());
            } catch (Exception e) {
                errorCount++;
                errorLines.add("General error processing line: \"" + line + "\", Message: " + e.getMessage());
                System.err.println("General error processing line: " + e.getMessage());
            }
        }

        // Create a summary report
        String report = String.format("Processing Complete:\n" +
                        "Created: %d\n" +
                        "Updated: %d\n" +
                        "Errors: %d\n" +
                        "Error Details: %s",
                createdCount, updatedCount, errorCount, errorLines);
        return report;
    }



    @Override
    public void verifyCardholder(VerifyCardholderRequest request) {
        // Send the request to the RabbitMQ exchange using the specific routing key
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, request);  // Message is converted to JSON
        System.out.println("Verification request sent for cardholder: " + request.getCardNumber());
    }
}
