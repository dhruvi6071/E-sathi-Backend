package com.example.ESathi.DTO.UserNeedDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalNofiticationDTO {
    private List<String> personalmessage ;
    private String billMessage;
    private String unitMessage;

}
