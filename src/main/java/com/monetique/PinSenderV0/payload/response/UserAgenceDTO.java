package com.monetique.PinSenderV0.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserAgenceDTO {

        // User details
        private Long userId;
        private String username;
        private String email;
        private String phoneNumber;


        // Agency details
        private Long agencyId;
        private String agencyName;
        private String agencyEmail;
        private String agencyCode;
        private String Region;
        private String city;
        private String agencyPhoneNumber;

        // Constructor
        public UserAgenceDTO(Long userId, String username, String email, String phoneNumber,
                             Long agencyId, String agencyName, String agencyEmail, String agencyCode,String city,String Region, String agencyPhoneNumber) {
                this.userId = userId;
                this.username = username;
                this.email = email;
                this.phoneNumber = phoneNumber;

                this.agencyId = agencyId;
                this.agencyName = agencyName;
                this.agencyEmail = agencyEmail;
                this.agencyCode = agencyCode;
                this.Region=Region;
                this.city=city;
                this.agencyPhoneNumber = agencyPhoneNumber;
        }


}
