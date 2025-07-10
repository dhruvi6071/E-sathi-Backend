package com.example.ESathi.authentication.DTO;

import com.example.ESathi.model.User;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken ;
    private String refreshToken;
    private String email;
    private String role;


}
