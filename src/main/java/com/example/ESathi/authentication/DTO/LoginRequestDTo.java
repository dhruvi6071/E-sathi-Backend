package com.example.ESathi.authentication.DTO;

import com.example.ESathi.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class LoginRequestDTo {
    private String email;
    private String password;
    private User.Role role ;
}
