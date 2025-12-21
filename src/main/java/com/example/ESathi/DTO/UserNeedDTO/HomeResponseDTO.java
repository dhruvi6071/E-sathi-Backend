package com.example.ESathi.DTO.UserNeedDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponseDTO {

    private String area;
    private String weather;
    private String risk ;
    private List<AreaHomeWarningDTO> areaHomeWarningDTOS;
    private double totalOneYearUnitConsumption;
    private LastPendingBillDTO lastPendingBillDTO;
    private  EngineerResponseForHomeDTO engineerResponseForHomeDTO ;
}
