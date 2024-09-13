package com.monetique.PinSenderV0.security.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RandomCardholderCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RandomCardholderCommandLineRunner.class);

    @Autowired
    private RandomCardholderService randomCardholderService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting RandomCardholderCommandLineRunner...");
        int numberOfCardholdersToGenerate = 10; // Set the desired number of cardholders to generate
        for (int i = 0; i < numberOfCardholdersToGenerate; i++) {
            randomCardholderService.generateRandomCardholder();
        }
        logger.info("Generated " + numberOfCardholdersToGenerate + " random cardholders.");
    }
}
