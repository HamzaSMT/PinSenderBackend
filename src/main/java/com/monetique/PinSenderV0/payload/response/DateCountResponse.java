package com.monetique.PinSenderV0.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DateCountResponse {
    private String date; // The date for which the statistics are reported
    private Long total;  // Total counts of OTPs and PINs for that date
}