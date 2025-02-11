package com.monetique.PinSenderV0.payload.response;


import com.monetique.PinSenderV0.models.Banks.TabBank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {



        private Long id;
        private String username;
        private String email;
        private String phoneNumber;
        private String role;
        private String bankName;
        private String bankCode;
        private byte[] logoContent;
        private boolean status;


}
