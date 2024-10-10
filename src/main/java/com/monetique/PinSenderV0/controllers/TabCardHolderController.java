package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Interfaces.ICardholderService;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.TabCardHolderresponse;
import com.monetique.PinSenderV0.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cardholders")
public class TabCardHolderController {

    @Autowired
    private ICardholderService cardHolderService;

    @GetMapping
    public ResponseEntity<List<TabCardHolderresponse>> getAllCardHolders() {
        List<TabCardHolderresponse> cardHolders = cardHolderService.getAllCardHolders();
        return ResponseEntity.ok(cardHolders);
    }


    @PostMapping("/upload")
    public void uploadCardHolderFile(@RequestParam("file") MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Send each line to the service for processing
                cardHolderService.processCardHolderLine(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @PostMapping("/upload2")
    public ResponseEntity<String> uploadCardHolderFile2(@RequestParam("file") MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read all lines from the file and store them in a list
            List<String> lines = reader.lines().collect(Collectors.toList());

            // Call the service to process the cardholder lines and generate a report
            String report = cardHolderService.processCardHolderLines(lines);

            // Return the generated report as the response
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing the file.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCardholder(@RequestBody VerifyCardholderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        // Set user details in the request
        request.setAgentId(currentUser.getId());
        request.setBranchId(currentUser.getAgency() != null ? currentUser.getAgency().getId() : null);
        request.setBankId(currentUser.getBank() != null ? currentUser.getBank().getId() : null);


        cardHolderService.verifyCardholder(request);
        return new ResponseEntity<>("Verification request sent to queue.", HttpStatus.ACCEPTED);
    }





    }