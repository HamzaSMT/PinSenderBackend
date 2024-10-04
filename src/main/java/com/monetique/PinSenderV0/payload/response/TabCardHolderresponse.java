package com.monetique.PinSenderV0.payload.response;


import com.monetique.PinSenderV0.models.Banks.TabBin;
import com.monetique.PinSenderV0.models.Banks.TabCardHolder;
import lombok.Getter;
import lombok.Setter;

    @Getter
    @Setter
    public class TabCardHolderresponse {
        private String clientNumber;
        private String cardNumber;
        private String creationDate;
        private String modificationDate;
        private String cancellationCode;
        private String name;
        private String companyName;
        private String agencyCode;
        private String rib;
        private String validationDate;
        private String finalDate;
        private String cardType;
        private String countryCode;
        private String nationalId;
        private String pinOffset;
        private String gsm;
        private String email;
        private String bankCode;  // To hold the bank code
        private TabBin binNumber;  // To hold the bin number

        public TabCardHolderresponse(TabCardHolder tabCardHolder) {
            this.clientNumber = tabCardHolder.getClientNumber();
            this.cardNumber = tabCardHolder.getCardNumber();
            this.creationDate = tabCardHolder.getCreationDate();
            this.modificationDate = tabCardHolder.getModificationDate();
            this.cancellationCode = tabCardHolder.getCancellationCode();
            this.name = tabCardHolder.getName();
            this.companyName = tabCardHolder.getCompanyName();
            this.agencyCode = tabCardHolder.getAgencyCode();
            this.rib = tabCardHolder.getRib();
            this.validationDate = tabCardHolder.getValidationDate();
            this.finalDate = tabCardHolder.getFinalDate();
            this.cardType = tabCardHolder.getCardType();
            this.countryCode = tabCardHolder.getCountryCode();
            this.nationalId = tabCardHolder.getNationalId();
            this.pinOffset = tabCardHolder.getPinOffset();
            this.gsm = tabCardHolder.getGsm();
            this.email = tabCardHolder.getEmail();
            this.bankCode = tabCardHolder.getBank() != null ? tabCardHolder.getBank().getBankCode() : null;  // Bank details
            this.binNumber = tabCardHolder.getBin() != null ? tabCardHolder.getBin() : null;  // Bin details
        }
    }







