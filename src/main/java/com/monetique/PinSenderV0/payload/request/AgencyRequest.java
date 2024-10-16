package com.monetique.PinSenderV0.payload.request;


import lombok.Data;

@Data
public class AgencyRequest {

    private String name;
    private String contactEmail;
    private String contactPhoneNumber;
    private String agencyCode;
    private String Region;
    private String city;
    private String adresse;

}
