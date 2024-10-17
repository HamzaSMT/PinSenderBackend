package com.monetique.PinSenderV0.payload.request;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BankRequest {


    private String name;
    private String bankCode;
    private String libelleBanque;
    private String enseigneBanque;
    private String ica;
    private String binAcquereurVisa;
    private String binAcquereurMcd;
    private String ctb;
    private boolean banqueEtrangere;



}






