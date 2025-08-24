package com.example.ESathi.authentication.contoller;

import com.example.ESathi.authentication.DTO.AuthResponseDTO;
import com.example.ESathi.authentication.DTO.LoginRequestDTo;
import com.example.ESathi.authentication.DTO.RegisterDTO;
import com.example.ESathi.authentication.Services.JwtTokenSerivce;
import com.example.ESathi.model.Stations;
import com.example.ESathi.model.User;
import com.example.ESathi.model.UserArea;
import com.example.ESathi.model.Village;
import com.example.ESathi.repositories.StationRepository;
import com.example.ESathi.repositories.UserAreaRepository;
import com.example.ESathi.repositories.UserRepository;
import com.example.ESathi.repositories.VillageRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;
import java.awt.geom.Area;
import java.security.Security;

@RestController
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private  AuthenticationManager authManager;

    private final JwtTokenSerivce jwtService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final VillageRepository villageRepository;
    private final UserAreaRepository userAreaRepository;
    private final StationRepository stationRepository ;

    public AuthController(JwtTokenSerivce jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          VillageRepository villageRepository,
                          UserAreaRepository userAreaRepository, StationRepository stationRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.villageRepository=villageRepository;
        this.userAreaRepository=userAreaRepository;
        this.stationRepository = stationRepository;
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTo request)
    {
        Authentication authentication=authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->{throw new UsernameNotFoundException("email not register");
                });

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken= jwtService.generateRefreshToken(authentication);
        return ResponseEntity.ok(new AuthResponseDTO(accessToken,refreshToken ,user.getEmail(),user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterDTO request)
    {
        System.out.println("inside register : ");

        try {
            User user = null;
            if (request.getRole() == User.Role.USER) {
                Village village = villageRepository.findByPincode(request.getPinCode())
                        .orElseThrow(() -> new UsernameNotFoundException("Enter correct pinoced"));

                System.out.println(village);

                UserArea area = userAreaRepository.findById(village.getUserArea().getUserAreaID())
                        .orElseThrow(() -> new UsernameNotFoundException("are id is not present"));

                Stations stations = stationRepository.findByName(request.getStationName());


                user = User.builder()
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .village(village)
                        .area(area)
                        .isActive(true)
                        .name(request.getName())
                        .role(User.Role.USER)
                        .assignStation(stations)
                        .build();
                userRepository.save(user);
            }
            if (request.getRole() == User.Role.ENGINEER) {
                Stations stations = stationRepository.findByName(request.getStationName());


                user = User.builder()
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .village(null)
                        .area(null)
                        .isActive(true)
                        .name(request.getName())
                        .role(User.Role.ENGINEER)
                        .assignStation(stations)
                        .build();
                userRepository.save(user);
            }

            return ResponseEntity.ok(user);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return ResponseEntity.ok(new User());
        }
    }
}
