package com.example.ESathi.DTO.UserNeedDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingBillDTO {
    private YearMonth billingMonth;
    private Double amount;
}
