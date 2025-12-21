package com.example.ESathi.authentication.Services;

import com.example.ESathi.model.User;
import com.example.ESathi.repositories.UserRepository;
import com.example.ESathi.utils.customeExeptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws ResourceNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("user not found with email" + email));


    }
}
