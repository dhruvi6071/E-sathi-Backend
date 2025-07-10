package com.example.ESathi.DTO.EngineerNeddDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPersonalNotificationDTO {

    private Long userId;
    private String message ;
    private String title ;
    private String type;

}
