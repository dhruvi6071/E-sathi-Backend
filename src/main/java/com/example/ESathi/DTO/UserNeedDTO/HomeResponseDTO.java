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

    private String are;
    private String weadher;
    private String risk ;
    private List<AreaHomeWarningDTO> areaHomeWarningDTOS;
    private PersonalNofiticationDTO personalNofiticationDTO;
    private List<PendingBillDTO> pendingBillDTO;
    private Map<YearMonth , String>  pendingBillMesaage;
}
