package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Interfaces.ICardholderService;
import com.monetique.PinSenderV0.models.Card.CardHolderLoadReport;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.TabCardHolderresponse;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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




    @PostMapping("/verify")
    public ResponseEntity<?> verifyCardholder(@RequestBody VerifyCardholderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        // Set user details in the request
        request.setAgentId(currentUser.getId());
        request.setBranchId(currentUser.getAgency() != null ? currentUser.getAgency().getId() : null);
        request.setBankId(currentUser.getBank() != null ? currentUser.getBank().getId() : null);


        cardHolderService.verifyCardholder(request);


        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponse("Verification request sent to queue.", 202));


    }

///////////////////////////////////////////////////load cards
@PostMapping("/upload")
public ResponseEntity<?> uploadCardHolderFile(@RequestParam("file") MultipartFile file) {
    try {
        // Check if the file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty.");
        }

        // Read lines from the uploaded file
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            lines = reader.lines().collect(Collectors.toList());
        }

        // Process the cardholder lines and log the result
        cardHolderService.processCardHolderLines(lines, file.getOriginalFilename());

        return ResponseEntity.ok("Cardholder file processed successfully.");
    } catch (Exception e) {

        return ResponseEntity.status(500).body(new MessageResponse("Error processing file", 500));
    }
}
// Endpoint to get all load reports
@GetMapping("/load-reports")
public ResponseEntity<List<CardHolderLoadReport>> getAllLoadReports() {
    List<CardHolderLoadReport> loadReports = cardHolderService.getAllLoadReports();
    return ResponseEntity.ok(loadReports);
}

    // Endpoint to get a specific load report by its ID
    @GetMapping("/load-reports/{id}")
    public ResponseEntity<CardHolderLoadReport> getLoadReportById(@PathVariable Long id) {
        CardHolderLoadReport loadReport = cardHolderService.getLoadReportById(id);

        return ResponseEntity.ok(loadReport);
    }




    ////////////////////////////////////////////////////////



/*
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
            String report = cardHolderService.processCardHolderLines(lines, file.getName());

            // Return the generated report as the response
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing the file.");
        }
    }
*/

    }