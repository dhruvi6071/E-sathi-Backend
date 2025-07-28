package com.example.ESathi.DTO.UserNeedDTO;

import com.example.ESathi.model.Bill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.pl.NIP;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingBillsResponseDTO {

    private Long billId;
    private String user ;
    private LocalDate billDate ;
    private YearMonth billingMonth ;
    private LocalDate dueDate ;
    private double amount ;
    private double unit ;
    private Bill.Status status ;
 }
