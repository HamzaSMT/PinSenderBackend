package com.monetique.PinSenderV0.tracking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LogScheduler {

    @Autowired
    private ItrackingingService apiRequestLogService;
    @Value("${log.directory}")
    private String externalLogDirectory;


    // Schedule this task to run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")  // Adjust cron expression as needed
    public void generateAndDeleteLogs() {
        List<ApiRequestLog> logs = apiRequestLogService.getAllLogs();

        if (!logs.isEmpty()) {
            // Get the current date to append to the filename
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String fileName = externalLogDirectory+"api_request_logs_" + currentDate + ".txt";
            // Generate the log file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                for (ApiRequestLog log : logs) {
                    writer.write(formatLogEntry(log));
                    writer.newLine();
                }
                System.out.println("Log file generated successfully at midnight.");
            } catch (IOException e) {
                e.printStackTrace();  // Handle the exception (e.g., logging)
            }

            // After generating the log file, delete all the logs
            apiRequestLogService.deleteAllLogs();
        }
    }


    // Helper method to format each log entry
    public String formatLogEntry(ApiRequestLog log) {
        StringBuilder sb = new StringBuilder();

        sb.append("Session ID: ").append(log.getSessionId()).append(System.lineSeparator());
        sb.append("Request Path: ").append(log.getRequestPath()).append(System.lineSeparator());
        sb.append("Method: ").append(log.getMethod()).append(System.lineSeparator());
        sb.append("Status Code: ").append(log.getStatusCode()).append(System.lineSeparator());
        sb.append("Response Time (ms): ").append(log.getResponseTimeMs()).append(System.lineSeparator());
        sb.append("Timestamp: ").append(log.getTimestamp()).append(System.lineSeparator());
        sb.append("Username: ").append(log.getUsername()).append(System.lineSeparator());
        sb.append("User Agent: ").append(log.getUserAgent()).append(System.lineSeparator());
        sb.append("Request Body: ").append(log.getRequestBody()).append(System.lineSeparator());
        sb.append("Response Body: ").append(log.getResponseBody()).append(System.lineSeparator());
        sb.append("Exception Message: ").append(log.getExceptionMessage()).append(System.lineSeparator());
        sb.append("Request Size: ").append(log.getRequestSize()).append(" bytes").append(System.lineSeparator());
        sb.append("Response Size: ").append(log.getResponseSize()).append(" bytes").append(System.lineSeparator());
        sb.append("IP Address: ").append(log.getIpAddress()).append(System.lineSeparator());
        sb.append("-------------------------------------------------------------").append(System.lineSeparator());

        return sb.toString();  // Return the formatted string for the log entry
    }

}
