package com.monetique.PinSenderV0.models.Statistique;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name = "sent_item_statistics")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor


    public class SentItemStatistics {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "bank_id", nullable = true)
        private Long bankId;

        @Column(name = "branch_id", nullable = true)
        private Long branchId;

        @Column(name = "agent_id", nullable = true)
        private Long agentId;

        @Column(name = "bank_name", nullable = true)
        private String bankName;

        @Column(name = "branch_name", nullable = true)
        private String branchName;

        @Column(name = "agent_name", nullable = true)
        private String agentName;

        @Column(name = "type", nullable = true)
        private String type; // "OTP" or "PIN"

        @Column(name = "date", nullable = false)
        private LocalDate date;

        @Column(name = "total_sent", nullable = false)
        private Long totalSent;

        public SentItemStatistics(Long bankId, Long branchId, Long agentId, String bankName, String branchName, String agentName, String type, LocalDate date, Long totalSent) {
            this.bankId = bankId;
            this.branchId = branchId;
            this.agentId = agentId;
            this.bankName = bankName;
            this.branchName = branchName;
            this.agentName = agentName;
            this.type = type;
            this.date = date;
            this.totalSent = totalSent;
        }
}

