package com.monetique.PinSenderV0.controllers;


import com.monetique.PinSenderV0.Interfaces.IStatisticservices;
import com.monetique.PinSenderV0.payload.response.AgentStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.BankStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.OverallStatisticsResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
   @Autowired
   private IStatisticservices statisticservices;



    @GetMapping("/bank/{bankId}")
    public ResponseEntity<?> getStatisticsForBank(@PathVariable Long bankId) {
        try {
            BankStatisticsResponse response = statisticservices.getStatisticsForBank(bankId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse ("Bank not found: " + bankId,404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body( new MessageResponse("Error fetching statistics for bank: " + e.getMessage(),500));
        }
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<?> getStatisticsForAgent(@PathVariable Long agentId) {
        try {
            AgentStatisticsResponse response = statisticservices.getStatisticsForAgent(agentId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Agent not found: " + agentId,404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching statistics for agent: " + e.getMessage(),500));
        }
    }

    @GetMapping("/overall")
    public ResponseEntity<?> getOverallStatistics() {
        try {
            OverallStatisticsResponse response = statisticservices.getOverallStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching overall statistics: " + e.getMessage(),500));
        }
    }
}

