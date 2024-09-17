package com.monetique.PinSenderV0.payload.request;

import lombok.Data;

@Data


    public class ChangePasswordRequest {
        private Long userId;
        private String oldPassword;
        private String newPassword;

}
