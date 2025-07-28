package com.example.ESathi.DTO.UserNeedDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastPendingBillDTO {
    private YearMonth billingMonth;
    private Double amount;
    private Double unitCousume ;
    private Date billdate;
    private Date dueDate;
    private String message ;
}
