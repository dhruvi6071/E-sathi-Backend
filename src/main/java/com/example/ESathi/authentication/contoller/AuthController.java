package com.example.ESathi.authentication.contoller;

import com.example.ESathi.authentication.DTO.AuthResponseDTO;
import com.example.ESathi.authentication.DTO.LoginRequestDTo;
import com.example.ESathi.authentication.DTO.RegisterDTO;
import com.example.ESathi.authentication.Services.JwtTokenSerivce;
import com.example.ESathi.model.Stations;
import com.example.ESathi.model.User;
import com.example.ESathi.model.Village;
import com.example.ESathi.repositories.StationRepository;
import com.example.ESathi.repositories.UserRepository;
import com.example.ESathi.repositories.VillageRepository;
import com.example.ESathi.utils.ApiResponse;
import com.example.ESathi.utils.customeExeptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.bouncycastle.asn1.x509.UserNotice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private  AuthenticationManager authManager;

    private final JwtTokenSerivce jwtService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final VillageRepository villageRepository;
    private final StationRepository stationRepository ;

    public AuthController(JwtTokenSerivce jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          VillageRepository villageRepository,
                           StationRepository stationRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.villageRepository=villageRepository;
        this.stationRepository = stationRepository;
    }


    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@RequestBody LoginRequestDTo request)
    {
        //Stores user’s authentication info.    //Validates the credentials with Spring Security
        Authentication authentication=authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String mail = request.getEmail();
        User user =  userRepository.findByEmail(mail)
                .orElseThrow(()-> new ResourceNotFoundException("User not present with name" + mail));

        //set authenticate use in context and making it accessible globally within the current thread
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken= jwtService.generateRefreshToken(authentication);
        return ApiResponse.success(new AuthResponseDTO(accessToken,refreshToken ,user.getEmail(),user.getRole().name()));
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterDTO request)
    {
        System.out.println("inside register : ");

        try {
            User user = null;
            if (User.Role.fromString(String.valueOf(request.getRole())) == User.Role.USER) {
                String pin = request.getPinCode() ;
                Village village = villageRepository.findByPincode(pin)
                        .orElseThrow(() -> new ResourceNotFoundException("Enter correct pin code, this is not valid" + pin ));

//                System.out.println(village);

                Long stationId =  village.getStations().getSationID() ;
                Stations stations = stationRepository.findById(stationId)
                        .orElseThrow(() -> new ResourceNotFoundException("station not Found with this Id" + stationId));


                user = User.builder()
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .village(village)
                        .isActive(true)
                        .name(request.getName())
                        .role(User.Role.USER)
                        .assignStation(stations)
                        .build();
                userRepository.save(user);
            }
            if (User.Role.fromString(String.valueOf(request.getRole())) == User.Role.ENGINEER) {
                String pin = request.getPinCode() ;
                Village village = villageRepository.findByPincode(pin)
                        .orElseThrow(() -> new ResourceNotFoundException("Enter correct pin code, this is not valid" + pin ));

//                System.out.println(village);

                Long stationId =  village.getStations().getSationID() ;
                Stations stations = stationRepository.findById(stationId)
                        .orElseThrow(() -> new ResourceNotFoundException("station not Found with this Id" + stationId));


                user = User.builder()
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .village(null)
                        .isActive(true)
                        .name(request.getName())
                        .role(User.Role.ENGINEER)
                        .assignStation(stations)
                        .build();
                userRepository.save(user);
            }

            System.out.println("registration success" +  request.getEmail());
            return ApiResponse.success(user);
        }
        catch (Exception e)
        {
            throw  new ResourceNotFoundException("Some Problem During Registration " + e.getMessage());
        }
    }
}
