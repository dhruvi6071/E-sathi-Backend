package com.example.ESathi.DTO.UserNeedDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsagePageResponseDTO {

    private Map<LocalDate, Double> unitWithDate ;
    private String geminiPrediction ;
    private double oneYearUnitAVG ;
    private double lastMonthUnitConsume;
    private String lastMonthBillStatus;
}
