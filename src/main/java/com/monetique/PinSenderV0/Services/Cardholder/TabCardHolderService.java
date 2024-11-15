package com.monetique.PinSenderV0.Services.Cardholder;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.ICardholderService;
import com.monetique.PinSenderV0.Interfaces.IbankService;
import com.monetique.PinSenderV0.Interfaces.ItabBinService;
import com.monetique.PinSenderV0.models.Card.CardHolderErrorDetail;
import com.monetique.PinSenderV0.models.Card.CardHolderLoadReport;
import com.monetique.PinSenderV0.models.Card.TabCardHolder;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.TabCardHolderresponse;
import com.monetique.PinSenderV0.repository.CardHolderLoadReportRepository;
import com.monetique.PinSenderV0.repository.TabCardHolderRepository;
import com.monetique.PinSenderV0.Services.managementbank.AgencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TabCardHolderService implements ICardholderService {

    private static final Logger logger = LoggerFactory.getLogger(AgencyService.class);

    @Autowired
    private TabCardHolderRepository tabCardHolderRepository;

    @Autowired
    private CardHolderLoadReportRepository cardHolderLoadReportRepository;
    @Autowired
    private IbankService ibankService;
    @Autowired
    private ItabBinService itabBinService;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    private static final String EXCHANGE_NAME = "cardholder.exchange";
    private static final String ROUTING_KEY = "cardholder.routingKey";;


    @Override
    public void verifyCardholder(VerifyCardholderRequest request) {
        // Send the request to the RabbitMQ exchange using the specific routing key
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, request);  // Message is converted to JSON
        System.out.println("Verification request sent for cardholder: " + request.getCardNumber());
    }

    @Override
    public List<TabCardHolderresponse> getAllCardHolders() {
        logger.info("getting list of cardholder ");
        List<TabCardHolder> cardHolders = tabCardHolderRepository.findAll();
        List<TabCardHolderresponse> response = cardHolders.stream()
                .map(TabCardHolderresponse::new)
                .collect(Collectors.toList());
        return response;
    }

    @Override
    public TabCardHolder getCardHolderByCardNumber(String cardNumber) {
        // Rechercher le cardholder par numéro de carte
        TabCardHolder cardHolder = tabCardHolderRepository.findByCardNumber(cardNumber);

        // Gérer le cas où le cardholder n'est pas trouvé
        if (cardHolder == null) {
            throw new ResourceNotFoundException("CardHolder", "cardNumber", cardNumber);
        }

        return cardHolder;
    }



    @Override
    public TabCardHolder extractCardHolderAttributes(String line) {
        TabCardHolder cardHolder = new TabCardHolder();
        logger.info("Extract attributes from specific positions in the line");

        // Extract attributes from specific positions in the line
        cardHolder.setClientNumber(line.substring(0, 24)); // Client number
        cardHolder.setCardNumber(line.substring(24, 43)); // Card number
        cardHolder.setName(line.substring(43, 69));
        cardHolder.setCompanyName(line.substring(69, 95));
        cardHolder.setBankCode(line.substring(95,100));
        cardHolder.setAgencyCode(line.substring(100, 105));
        cardHolder.setRib(line.substring(105, 129));
        cardHolder.setFinalDate(line.substring(129, 133));
        cardHolder.setCardType(line.substring(133, 135));
        cardHolder.setCountryCode(line.substring(135, 138));
        cardHolder.setNationalId(line.substring(138, 154));
        cardHolder.setPinOffset(line.substring(154,166));// National ID
        cardHolder.setGsm(line.substring(166, 181)); // GSM
        cardHolder.setEmail(line.substring(181, 231));
        cardHolder.setBin(itabBinService.getbinbybinnumber(line.substring(231,239)));
        // Add other field extractions here (e.g., bin, bank, etc.)

        return cardHolder;
    }
    @Override
    public void updateCardHolder(TabCardHolder existingCardHolder, TabCardHolder updatedCardHolder) {
        // Update only the fields that are necessary
        logger.info("Update only the fields that are necessary");
        existingCardHolder.setName(updatedCardHolder.getName());
        existingCardHolder.setCompanyName(updatedCardHolder.getCompanyName());
        existingCardHolder.setNationalId(updatedCardHolder.getNationalId());
        existingCardHolder.setRib(updatedCardHolder.getRib());
        existingCardHolder.setFinalDate(updatedCardHolder.getFinalDate());
        existingCardHolder.setCompanyName(updatedCardHolder.getCompanyName());
        existingCardHolder.setAgencyCode(updatedCardHolder.getAgencyCode());
        existingCardHolder.setGsm(updatedCardHolder.getGsm());
        existingCardHolder.setEmail(updatedCardHolder.getEmail());

        // Save updated cardholder
        try {
            tabCardHolderRepository.save(existingCardHolder);
            logger.info("Save updated cardholder");
        } catch (DataIntegrityViolationException e) {

            logger.error("Error while updating cardholder",e.getMessage());
            // Handle the error (e.g., log it or rethrow it)
        }
    }
    @Override
    public void processCardHolderLine(String line) {
        // Extract the card number and other attributes from the line once

        TabCardHolder cardHolderAttributes = extractCardHolderAttributes(line);
        String cardNumber = cardHolderAttributes.getCardNumber();  // Extract the card number
        String numclient= cardHolderAttributes.getClientNumber();
        logger.info("Processing Card Holder with card number: ",cardNumber);
        // Check if the cardholder already exists in the system
        TabCardHolder existingCardHolder = tabCardHolderRepository.findByClientNumber(numclient);

        if (existingCardHolder != null) {
            // If the cardholder exists, update their details
            System.out.println("Cardholder exists, updating details for card number: " + cardNumber);
            updateCardHolder(existingCardHolder, cardHolderAttributes);
        } else {
            // If the cardholder doesn't exist, create a new one
            System.out.println("Cardholder doesn't exist, creating new record for card number: " + cardNumber);
            tabCardHolderRepository.save(cardHolderAttributes);
        }
    }


    @Override
    public void processCardHolderLines(List<String> lines, String fileName) {
        int createdCount = 0;
        int updatedCount = 0;
        int errorCount = 0;
        List<CardHolderErrorDetail> errorDetails = new ArrayList<>();  // List to track errors


        // Loop through each line to process cardholders
        for (String line : lines) {
            try {
                TabCardHolder cardHolderAttributes = extractCardHolderAttributes(line);
                TabCardHolder existingCardHolder = tabCardHolderRepository.findByClientNumber(cardHolderAttributes.getClientNumber());

                if (existingCardHolder != null) {
                    updateCardHolder(existingCardHolder, cardHolderAttributes);
                    updatedCount++;
                } else {
                    tabCardHolderRepository.save(cardHolderAttributes);
                    createdCount++;
                }
            } catch (DataIntegrityViolationException e) {
                errorCount++;
                String cardNumber = line.substring(24, 43);  // Assuming card number is in these positions
                String truncatedMessage = e.getMessage().length() > 100 ? e.getMessage().substring(0, 100) : e.getMessage();
                errorDetails.add(new CardHolderErrorDetail(cardNumber, truncatedMessage));

                //errorDetails.append("Card Number: ").append(cardNumber).append(", Error: ").append(e.getMessage()).append("\n");
            } catch (Exception e) {
                errorCount++;
                String cardNumber = line.substring(24, 43);  // Assuming card number is in these positions
                String truncatedMessage = e.getMessage().length() > 100 ? e.getMessage().substring(0, 100) : e.getMessage();
                errorDetails.add(new CardHolderErrorDetail(cardNumber, truncatedMessage));

            }
        }

        // Save the report after processing the batch
        CardHolderLoadReport report = new CardHolderLoadReport(fileName, createdCount, updatedCount, errorDetails);
        cardHolderLoadReportRepository.save(report);
    }

@Override
public CardHolderLoadReport getLoadReportById(Long id) {
        return cardHolderLoadReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoadReport", "id", id));
    }
@Override
public List<CardHolderLoadReport> getAllLoadReports() {
        return cardHolderLoadReportRepository.findAll();
    }



}
