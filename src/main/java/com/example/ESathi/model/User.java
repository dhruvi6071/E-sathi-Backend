package com.example.ESathi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)  // Better for auto-increment primary keys
    private Long userID;

    @Column(nullable = false)
    private String name;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Pattern(regexp = "\\d{10}", message = "phone number must be 10 digits")
    @Column(nullable = false, unique = true)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "villageId")
    private Village village;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "areaID")
    private UserArea area;

    @ManyToOne
    @JoinColumn(name = "stationID" , nullable = false)
    private Stations assignStation;

    @Column(nullable = false)
    private boolean isActive;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.name()));
    }


    @Override
    public String getUsername() {
        return this.email;  // Use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Customize if you have account expiration logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Customize if you have lock logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Customize if you have credential expiration logic
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public enum Role {
        USER,
        ENGINEER,
        ADMIN;
    }

}
