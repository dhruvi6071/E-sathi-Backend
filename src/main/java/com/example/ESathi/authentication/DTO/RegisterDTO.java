package com.example.ESathi.authentication.DTO;

import com.example.ESathi.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    private String name ;
    private String email ;
    private String phone ;
    private String pinCode;
    private String password;
    private User.Role role;
    private String stationName;
}
