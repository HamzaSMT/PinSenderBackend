package com.monetique.PinSenderV0.tracking;


import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.models.Users.UserSession;
import com.monetique.PinSenderV0.repository.UserSessionRepository;
import com.monetique.PinSenderV0.tracking.payload.ApiReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrackingService implements ItrackingingService {

    @Autowired
    private ApiRequestLogRepository apiRequestLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;
    @Override
    public UserSession getSessionById(Long sessionId) {
        // Fetch session from the repository by ID
        return userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
    }

@Override
public ApiReportResponse generateUserReport(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ApiRequestLog> logs = apiRequestLogRepository.findBySession_User_Id(userId).stream()
                .filter(log -> log.getTimestamp().isAfter(startDate) && log.getTimestamp().isBefore(endDate))
                .collect(Collectors.toList());

        long totalRequests = logs.size();
        long successRequests = logs.stream().filter(log -> log.getStatusCode() >= 200 && log.getStatusCode() < 300).count();
        long failedRequests = totalRequests - successRequests;

        return new ApiReportResponse(totalRequests, successRequests, failedRequests, logs);
    }

    // Generate report for all API calls by all users of an admin
    @Override
    public ApiReportResponse generateAdminReport(Long adminId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ApiRequestLog> logs = apiRequestLogRepository.findBySession_User_Admin_Id(adminId).stream()
                .filter(log -> log.getTimestamp().isAfter(startDate) && log.getTimestamp().isBefore(endDate))
                .collect(Collectors.toList());

        long totalRequests = logs.size();
        long successRequests = logs.stream().filter(log -> log.getStatusCode() >= 200 && log.getStatusCode() < 300).count();
        long failedRequests = totalRequests - successRequests;

        return new ApiReportResponse(totalRequests, successRequests, failedRequests, logs);
    }
@Override
public Map<Long, Long> generateSessionDurations(Long userId) {
        List<UserSession> sessions = userSessionRepository.findByUser_Id(userId);

        return sessions.stream().collect(Collectors.toMap(
                UserSession::getId,
                session -> session.getLogoutTime().isAfter(session.getLoginTime()) ?
                        session.getLogoutTime().atZone(ZoneId.systemDefault()).toEpochSecond() -
                                session.getLoginTime().atZone(ZoneId.systemDefault()).toEpochSecond() : 0
        ));
    }

    // Fetch logs by admin ID
    @Override
    public List<ApiRequestLog> getLogsByAdminId(Long adminId) {
        return apiRequestLogRepository.findBySession_User_Admin_Id(adminId);
    }

    // Fetch logs by user ID
    @Override
    public List<ApiRequestLog> getLogsByUserId(Long userId) {
        return apiRequestLogRepository.findBySession_User_Id(userId);
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
public void logRequest(UserSession session, String requestPath, HttpMethodEnum method, int statusCode, long responseTimeMs) {
        ApiRequestLog requestLog = new ApiRequestLog();
        requestLog.setSession(session);  // Correctly set the UserSession object
        requestLog.setRequestPath(requestPath);
        requestLog.setMethod(method);  // Assuming HttpMethodEnum is defined correctly
        requestLog.setStatusCode(statusCode);
        requestLog.setResponseTimeMs(responseTimeMs);
        requestLog.setTimestamp(LocalDateTime.now());

        // Save the log to the database
        apiRequestLogRepository.save(requestLog);
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

    @Override
    public UserSession getActiveSessionByUsername(String username) {
        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Find the active session for the user (where logoutTime is null)
        return userSessionRepository.findByUserAndLogoutTimeIsNull(user)
                .orElse(null);  // Return null if no active session is found
    }

}