package com.monetique.PinSenderV0.payload.response;

import com.monetique.PinSenderV0.models.Banks.TabBin;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Getter
@Setter
public class BinDTOresponse {
    private Long id;
    private String bin;
    private String systemCode;
    private String cardType;
    private String serviceCode;
    private String keyDataA;
    private String keyDataB;
    private String bankName;
    private String bankCode;


    public BinDTOresponse(TabBin tabBin) {
        this.id = tabBin.getId();
        this.bin = tabBin.getBin();
        this.systemCode = tabBin.getSystemCode();
        this.cardType = tabBin.getCardType();
        this.serviceCode = tabBin.getServiceCode();
        this.keyDataA=tabBin.getKeyDataA();
        this.keyDataB=tabBin.getKeyDataB();
        this.bankName = tabBin.getBank().getName();  // Bank details
        this.bankCode = tabBin.getBank().getBankCode();
    }

}