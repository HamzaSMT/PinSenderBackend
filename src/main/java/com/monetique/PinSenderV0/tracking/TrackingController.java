package com.monetique.PinSenderV0.tracking;
import com.monetique.PinSenderV0.models.Users.UserSession;
import com.monetique.PinSenderV0.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;



    // API to track active sessions (users currently logged in) - only accessible by Super Admin
    @GetMapping("/activeSessions")
    public ResponseEntity<?> trackActiveSessions() {
        // Check if the currently authenticated user is a Super Admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!currentUserDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"))) {

            throw new AccessDeniedException("Error: Only Super Admin can access this monitoring API.");
        }

        List<UserSession> activeSessions = trackingService.getActiveSessions();
        return ResponseEntity.ok(activeSessions);
    }

    // API to track all sessions across the system - only accessible by Super Admin
    @GetMapping("/allSessions")
    public ResponseEntity<?> trackAllSessions() {
        // Check if the currently authenticated user is a Super Admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!currentUserDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"))) {

            throw new AccessDeniedException("Error: Only Super Admin can access this monitoring API.");
        }

        List<UserSession> allSessions = trackingService.getAllSessions();
        return ResponseEntity.ok(allSessions);
    }

    @GetMapping("/logs/all")
    public List<ApiRequestLog> getAllLogs() {
        return trackingService.getAllLogs();
    }
    @GetMapping("/activities/user")
    public ResponseEntity<?> getActivitiesByUserAndDate(
            @RequestParam String username,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ApiRequestLog> activities = trackingService.getActivitiesByUserAndDate(username, date);
        if (activities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No activities found for user: " + username + " on date: " + date);
        }
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/most-used-apis")
    public ResponseEntity<?> getMostUsedApis() {
        Map<String, Long> mostUsedApis = trackingService.getMostUsedApis();
        if (mostUsedApis.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No API usage data available.");
        }
        return ResponseEntity.ok(mostUsedApis);
    }

    @GetMapping("/api-performance")
    public ResponseEntity<?> getApiPerformanceStatistics() {
        Map<String, Double> performanceStats = trackingService.getApiPerformanceStats();
        if (performanceStats.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No API performance data available.");
        }
        return ResponseEntity.ok(performanceStats);
    }

    @GetMapping("/average-response-time")
    public ResponseEntity<String> getAverageResponseTime() {
        double averageResponseTime = trackingService.getAverageResponseTime();
        return ResponseEntity.ok("Average response time: " + averageResponseTime + " ms");
    }

    @GetMapping("/error-count")
    public ResponseEntity<String> getErrorCount() {
        long errorCount = trackingService.getErrorCount();
        return ResponseEntity.ok("Total error count: " + errorCount);
    }

    @GetMapping("/api-request-distribution/hour")
    public ResponseEntity<Map<String, Object>> getApiRequestDistributionByHour() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = trackingService.getApiRequestDistributionByHour();

            if (result.isEmpty()) {
                // If no data found, return an appropriate response
                response.put("message", "No API requests found for the given period");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // If data exists, return the result
            response.put("hourlyDistribution", result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Handle any errors
            response.put("message", "An error occurred while retrieving API request distribution");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
