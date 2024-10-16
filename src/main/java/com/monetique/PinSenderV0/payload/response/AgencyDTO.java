package com.monetique.PinSenderV0.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor

    public class AgencyDTO {
        private Long id;
        private String name;
        private String contactEmail;
        private String agencyCode;
        private String contactPhoneNumber;
        private String adresse;
        private String Region;
        private String city;
        private String bankName;  // This will hold the bank name
       // This will hold the list of users

    }

