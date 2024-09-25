package com.monetique.PinSenderV0.payload.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {



        private Long id;
        private String username;
        private String password;
        private String email;
        private String phoneNumber;
        private String role;
        private String bankName;


}
