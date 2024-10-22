package com.monetique.PinSenderV0.tracking;
import com.monetique.PinSenderV0.models.Users.UserSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItrackingingService {
    UserSession getSessionById(Long sessionId);

    // Generate report for all API calls by a user
   // ApiReportResponse generateUserReport(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Generate report for all API calls by all users of an admin
    //ApiReportResponse generateAdminReport(Long adminId, LocalDateTime startDate, LocalDateTime endDate);

    // Generate report for session durations
    //Map<Long, Long> generateSessionDurations(Long userId);

    // Fetch logs by admin ID
    //List<ApiRequestLog> getLogsByAdminId(Long adminId);

    // Fetch logs by user ID
   // List<ApiRequestLog> getLogsByUserId(Long userId);

    // Fetch active sessions (users currently logged in)
    List<UserSession> getActiveSessions();

    // Fetch all sessions
    List<UserSession> getAllSessions();

    //void logRequest(UserSession session, String requestPath, HttpMethodEnum method, int statusCode, long responseTimeMs);

    void logRequest(Long sessionid,
                    String requestPath,
                    HttpMethodEnum method,
                    int statusCode,
                    long responseTimeMs,
                    String ipAddress,
                    String userAgent,
                    long requestSize,
                    long responseSize,
                    String exceptionMessage,String requestBody,  String responseBody );

    long getCountActiveSessions();

    List<ApiRequestLog> getActivitiesByUserAndDate(String username, LocalDate date);

    Map<String, Long> getMostUsedApis();

    Map<String, Double> getApiPerformanceStats();

    double getAverageResponseTime();

    long getErrorCount();


    Map<String, Object> getApiRequestDistributionByHour();



    Page<ApiRequestLog> getAllNonGetLogs(Pageable pageable);

    List<ApiRequestLog> getAllLogs();

    void deleteAllLogs();

    // Method to start a new session when the user logs in
    UserSession startSession(Long userId);

    void endSession(long sessionId);

    UserSession getActiveSessionByUsername(String username);


}
