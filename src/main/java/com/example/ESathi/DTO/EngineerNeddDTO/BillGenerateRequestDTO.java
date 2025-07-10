package com.example.ESathi.DTO.EngineerNeddDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillGenerateRequestDTO {

    private Long userId;
    private Double unitsUsed;
    private YearMonth month ;
}
