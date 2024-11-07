package com.monetique.PinSenderV0.tracking;


import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.models.Users.UserSession;
import com.monetique.PinSenderV0.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class TrackingService implements ItrackingingService {

    @Autowired
    private ApiRequestLogRepository apiRequestLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;


    @Override
    public void logRequest(Long sessionId,
                           String requestPath,
                           HttpMethodEnum method,
                           int statusCode,
                           long responseTimeMs,
                           String ipAddress,
                           String userAgent,
                           long requestSize,
                           long responseSize,
                           String exceptionMessage,
                           String requestBody,
                           String responseBody) {

        ApiRequestLog requestLog = new ApiRequestLog();

        // Fetch the session details if sessionId is valid
        UserSession session = (sessionId != null && sessionId > 0) ? getSessionById(sessionId) : null;
        requestLog.setSessionId(sessionId);
        // Set session details and username in one go
        if (session != null) {
            requestLog.setUsername(session.getUser() != null ? session.getUser().getUsername() : null);
        } else {
            requestLog.setSessionId(0); // Default session ID if session is not present
            requestLog.setUsername("user not yet authenticated"); // Username is null if session is not present
        }

        // Set other details
        requestLog.setRequestPath(requestPath);
        requestLog.setMethod(method);
        requestLog.setStatusCode(statusCode);
        requestLog.setResponseTimeMs(responseTimeMs);
        requestLog.setTimestamp(LocalDateTime.now());
        requestLog.setIpAddress(ipAddress);
        requestLog.setUserAgent(userAgent);
        requestLog.setRequestSize(requestSize);
        requestLog.setResponseSize(responseSize);
        requestLog.setExceptionMessage(exceptionMessage);
        requestLog.setRequestBody(requestBody);
        requestLog.setResponseBody(responseBody);

        // Save the log to the database
        apiRequestLogRepository.save(requestLog);
    }







    @Override
    public UserSession getActiveSessionByUsername(String username) {
        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Find the active session for the user (where logoutTime is null)
        return userSessionRepository.findByUserAndLogoutTimeIsNull(user)
                .orElse(null);  // Return null if no active session is found
    }

    @Override
    public UserSession getSessionById(Long sessionId) {
        // Fetch session from the repository by ID
        return userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
    }



    // Fetch active sessions (users currently logged in)
    @Override
    public List<UserSession> getActiveSessions() {
        return userSessionRepository.findByLogoutTimeIsNull();
    }

    // Fetch all sessions
    @Override
    public List<UserSession> getAllSessions() {
        return userSessionRepository.findAll();
    }
   @Override
   public long getCountActiveSessions() {
        return userSessionRepository.countActiveSessions(); // Assuming you have this method in your repository
    }

    @Override
    public List<ApiRequestLog> getActivitiesByUserAndDate(String username, LocalDate date) {
        return apiRequestLogRepository.findByUsernameAndDate(username, date); // Implement this method in your repository
    }
    @Override
    public Map<String, Long> getMostUsedApis() {
        return apiRequestLogRepository.findAll().stream()
                .collect(Collectors.groupingBy(ApiRequestLog::getRequestPath, Collectors.counting()));
    }
    @Override
    public Map<String, Double> getApiPerformanceStats() {
        return apiRequestLogRepository.findAll().stream()
                .collect(Collectors.groupingBy(ApiRequestLog::getRequestPath,
                        Collectors.averagingLong(ApiRequestLog::getResponseTimeMs)));
    }
    @Override
    public double getAverageResponseTime() {
        return apiRequestLogRepository.findAll().stream()
                .mapToLong(ApiRequestLog::getResponseTimeMs)
                .average()
                .orElse(0.0);
    }
    @Override
    public long getErrorCount() {
        return apiRequestLogRepository.findAll().stream()
                .filter(log -> log.getStatusCode() >= 400) // HTTP error codes
                .count();
    }
    @Override
    public Map<String, Object> getApiRequestDistributionByHour() {
        List<ApiRequestLog> logs = apiRequestLogRepository.findAll();

        // Grouping by hour first
        Map<Integer, List<ApiRequestLog>> logsByHour = logs.stream()
                .collect(Collectors.groupingBy(log -> log.getTimestamp().getHour()));

        // Preparing the result map
        List<Map<String, Object>> hourlyDistribution = new ArrayList<>();

        // Iterating over each hour
        for (Map.Entry<Integer, List<ApiRequestLog>> entry : logsByHour.entrySet()) {
            int hour = entry.getKey();
            List<ApiRequestLog> logsForHour = entry.getValue();

            // Grouping by API path within each hour
            Map<String, Long> apiRequestsByPath = logsForHour.stream()
                    .collect(Collectors.groupingBy(ApiRequestLog::getRequestPath, Collectors.counting()));

            // Creating the structure for each hour
            Map<String, Object> hourDetails = new HashMap<>();
            hourDetails.put("hour", hour);
            hourDetails.put("totalRequests", logsForHour.size());  // Total requests in that hour

            // Creating the list of API requests with path and count
            List<Map<String, Object>> apiRequestsList = new ArrayList<>();
            for (Map.Entry<String, Long> apiEntry : apiRequestsByPath.entrySet()) {
                Map<String, Object> apiDetails = new HashMap<>();
                apiDetails.put("apiPath", apiEntry.getKey());  // API path
                apiDetails.put("count", apiEntry.getValue());  // Count of requests for that API path
                apiRequestsList.add(apiDetails);
            }

            hourDetails.put("apiRequests", apiRequestsList);
            hourlyDistribution.add(hourDetails);
        }

        // Return the result map containing the hourly distribution
        Map<String, Object> result = new HashMap<>();
        result.put("hourlyDistribution", hourlyDistribution);
        return result;
    }
    @Override
    public Page<ApiRequestLog> getAllNonGetLogs(Pageable pageable) {
        Pageable sortedByTimestampDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("timestamp").descending()
        );
        return apiRequestLogRepository.findByMethodNot(HttpMethodEnum.GET, sortedByTimestampDesc);
    }
    @Override
    public List<ApiRequestLog> getAllLogs() {
        return apiRequestLogRepository.findAll();
    }

    @Override
    public void deleteAllLogs() {
        apiRequestLogRepository.deleteAll();
    }

    // Method to start a new session when the user logs in
    @Override
    public UserSession startSession(Long userId) {
        // Retrieve the user from the repository
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create a new session for the user
        UserSession session = new UserSession();
        session.setUser(user);
        session.setLoginTime(LocalDateTime.now());

        // Save the session to the repository
        return userSessionRepository.save(session);
    }


    @Override
    public void endSession(long sessionId) {
        // Find the session by sessionId and mark it as ended
        Optional<UserSession> session = userSessionRepository.findById(sessionId);
        if (session.isPresent()) {
            UserSession userSession = session.get();
            userSession.setLogoutTime(LocalDateTime.now());
            userSessionRepository.save(userSession);
        }
    }
}
