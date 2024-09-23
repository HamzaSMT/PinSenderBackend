package com.monetique.PinSenderV0.payload.request;


import lombok.Data;

@Data
public class BankRequest {


    private String name;
    private String codeBanque;
    private String libelleBanque;
    private String enseigneBanque;
    private String ica;
    private String binAcquereurVisa;
    private String binAcquereurMcd;
    private String ctb;
    private String banqueEtrangere;


}






