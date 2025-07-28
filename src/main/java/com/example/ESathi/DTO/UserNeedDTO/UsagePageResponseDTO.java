package com.example.ESathi.DTO.UserNeedDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsagePageResponseDTO {

    private Map<LocalDateTime , Double> unitWithDate ;
    private String lastBillAvgMessage ;
    private String geminiPrediction ;
}
