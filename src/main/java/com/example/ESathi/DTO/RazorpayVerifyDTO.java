package com.example.ESathi.DTO;

import lombok.Data;

@Data
public class RazorpayVerifyDTO {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String method; // UPI, CARD, NET_BANKING
    private String transactionRef;

}
