package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.models.Cardholder;
import com.monetique.PinSenderV0.repository.CardholderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Random;

@Service
public class RandomCardholderService {

    @Autowired
    private CardholderRepository cardholderRepository;

    private static final String CHARACTERS = "0123456789";
    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int CIN_LENGTH = 8;
    private static final int PIN_LENGTH = 4;
    private static final Random RANDOM = new Random();

    public Cardholder generateRandomCardholder() {
        String cardNumber = generateRandomString(CARD_NUMBER_LENGTH);
        String cin = generateRandomString(CIN_LENGTH);
        String phoneNumber = generateRandomPhoneNumber();
        String pin = generateRandomString(PIN_LENGTH);
        String expirationDate = generateRandomExpirationDate();

        Cardholder cardholder = new Cardholder(null, cardNumber, cin, phoneNumber, pin, expirationDate);
        return cardholderRepository.save(cardholder);
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private String generateRandomPhoneNumber() {
        // Generate random phone number with the format 216xxxxxxx
        return "216" + String.format("%07d", RANDOM.nextInt(10000000));
    }

    private String generateRandomExpirationDate() {
        // Generate a random future year and month for the expiration date
        int currentYear = YearMonth.now().getYear() % 100; // Get last two digits of current year
        int year = currentYear + RANDOM.nextInt(10); // Expiration year within the next 10 years
        int month = RANDOM.nextInt(12) + 1; // Random month between 1 and 12
        return String.format("%02d%02d", year, month); // Format as YYMM
    }
}
