package com.example.ESathi.DTO;

import com.example.ESathi.model.Payments;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillReceiptResponseDTO {

    private Long userId;
    private  String userName;
    private LocalDateTime billPaymentDate ;
    private Double unitConsume ;
    private Double amountPaid;
    private String transacrtionalRef;
    private Payments.Methods method;
    private YearMonth billMoth ;
    private  String status  ;


}
