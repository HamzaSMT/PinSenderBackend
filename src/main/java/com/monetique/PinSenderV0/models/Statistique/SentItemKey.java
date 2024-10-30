package com.monetique.PinSenderV0.models.Statistique;


import java.util.Objects;

public class SentItemKey {
    private Long bankId;
    private Long branchId;
    private Long agentId;
    private String type;

    public SentItemKey(Long bankId, Long branchId, Long agentId, String type) {
        this.bankId = bankId;
        this.branchId = branchId;
        this.agentId = agentId;
        this.type = type;
    }

    // Getters
    public Long getBankId() {
        return bankId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public String getType() {
        return type;
    }

    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SentItemKey)) return false;
        SentItemKey that = (SentItemKey) o;
        return Objects.equals(bankId, that.bankId) &&
                Objects.equals(branchId, that.branchId) &&
                Objects.equals(agentId, that.agentId) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bankId, branchId, agentId, type);
    }
}
