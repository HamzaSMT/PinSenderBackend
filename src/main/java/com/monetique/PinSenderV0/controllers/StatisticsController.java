package com.monetique.PinSenderV0.controllers;


import com.monetique.PinSenderV0.Interfaces.IStatisticservices;
import com.monetique.PinSenderV0.payload.response.AgentStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.BankStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.OverallStatisticsResponse;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getStatisticsForBank(@PathVariable Long bankId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long authBankId = currentUserDetails.getBank().getId();

            // If the requested bankId does not match the authenticated user's bankId, deny access
            if (!bankId.equals(authBankId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Access denied for this bank", 403));
            }

            BankStatisticsResponse response = statisticservices.getStatisticsForBank(authBankId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Bank not found: " + bankId, 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching statistics for bank: " + bankId, 500));
        }
    }


    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getStatisticsForAgent(@PathVariable Long agentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long authAgentId = currentUserDetails.getId();

            // If the requested agentId does not match the authenticated user's ID, deny access
            if (!agentId.equals(authAgentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Access denied for agent: " + agentId, 403));
            }

            AgentStatisticsResponse response = statisticservices.getStatisticsForAgent(authAgentId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Agent not found: " + agentId, 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching statistics for agent: " + agentId, 500));
        }
    }

    @GetMapping("/overall")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getOverallStatistics() {
        try {
            OverallStatisticsResponse response = statisticservices.getOverallStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error fetching overall statistics: ",500));
        }
    }
}

