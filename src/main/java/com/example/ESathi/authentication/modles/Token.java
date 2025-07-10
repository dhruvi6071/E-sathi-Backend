package com.example.ESathi.authentication.modles;

import com.example.ESathi.authentication.contoller.AuthController;
import com.example.ESathi.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long tokenId;
    private String token;
    private String tokenType;

    private String userName;
    private LocalDateTime expiryDate ;
    private boolean revoked ;

    public boolean isRevoked()
    {
        return revoked;
    }

}
