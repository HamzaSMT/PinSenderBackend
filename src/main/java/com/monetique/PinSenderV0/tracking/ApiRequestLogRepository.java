package com.monetique.PinSenderV0.tracking;



import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApiRequestLogRepository extends JpaRepository<ApiRequestLog, Long> {
    // Find activities by username and date
    @Query("SELECT log FROM ApiRequestLog log WHERE log.username = :username AND DATE(log.timestamp) = :date")
    List<ApiRequestLog> findByUsernameAndDate(String username, LocalDate date);

    // Find all logs by request path and group by path (used for most-used APIs)
    @Query("SELECT log.requestPath, COUNT(log) FROM ApiRequestLog log GROUP BY log.requestPath ORDER BY COUNT(log) DESC")
    List<Object[]> findMostUsedApis();

    // Find average response time by request path
    @Query("SELECT log.requestPath, AVG(log.responseTimeMs) FROM ApiRequestLog log GROUP BY log.requestPath")
    List<Object[]> findApiPerformanceStats();

    // Find total error count (status codes >= 400)
    @Query("SELECT COUNT(log) FROM ApiRequestLog log WHERE log.statusCode >= 400")
    long countErrors();

    // Find API request distribution by hour
    @Query("SELECT HOUR(log.timestamp), COUNT(log) FROM ApiRequestLog log GROUP BY HOUR(log.timestamp)")
    List<Object[]> findApiRequestDistributionByHour();

    Page<ApiRequestLog> findByMethodNot(HttpMethodEnum method, Pageable pageable);
}
