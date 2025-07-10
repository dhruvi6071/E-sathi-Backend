package com.example.ESathi.DTO.EngineerNeddDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaWarringRequestDTO {

    private  String stationName;
    private String message ;
    private LocalDateTime solveDate;
    private LocalDateTime startDate ;
}
