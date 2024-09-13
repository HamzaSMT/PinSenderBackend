package com.monetique.PinSenderV0.payload.response;



import com.monetique.PinSenderV0.models.ApiRequestLog;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ApiReportResponse {

    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private List<ApiRequestLog> logs;
}
