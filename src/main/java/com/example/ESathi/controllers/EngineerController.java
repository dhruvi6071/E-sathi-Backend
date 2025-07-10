package com.example.ESathi.controllers;

import com.example.ESathi.DTO.EngineerNeddDTO.AreaWarringRequestDTO;
import com.example.ESathi.DTO.EngineerNeddDTO.BillGenerateRequestDTO;
import com.example.ESathi.DTO.CommonResponseDTO;
import com.example.ESathi.DTO.EngineerNeddDTO.UserPersonalNotificationDTO;
import com.example.ESathi.Serivces.EngineerSerivce;
import com.example.ESathi.model.Bill;
import com.example.ESathi.model.Notification;
import com.example.ESathi.model.Outage;
import com.example.ESathi.model.User;
import com.example.ESathi.repositories.OutageRepository;
import com.example.ESathi.repositories.StationRepository;
import com.example.ESathi.repositories.UserAreaRepository;
import com.example.ESathi.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/engineer")
public class EngineerController {

    private  final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final OutageRepository outageRepository;
    private final EngineerSerivce engineerSerivce;

    public EngineerController(UserAreaRepository userAreaRepository,
                              StationRepository stationRepository,
                              UserRepository userRepository,
                              OutageRepository outageRepository,
                              EngineerSerivce engineerSerivce) {
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
        this.outageRepository = outageRepository;
        this.engineerSerivce = engineerSerivce;
    }



    //generate notification for ear warring
    @PostMapping("/areWarning")
    public ResponseEntity<CommonResponseDTO> addAreMessge(@RequestBody AreaWarringRequestDTO dto,Principal principal)
    {

        //get existing engineer name
           String name = principal.getName();

            Outage outage = engineerSerivce.generateNewOutage(dto , name);

            return ResponseEntity.ok().body(new CommonResponseDTO("outage is created "+ outage ));

    }


    //genreate BiLL
    @PostMapping("/bill")
    public ResponseEntity<CommonResponseDTO> createBill(@RequestBody BillGenerateRequestDTO dto, Principal principal)
    {
        User user= userRepository.findById(dto.getUserId())
                .orElseThrow(()-> new UsernameNotFoundException("please check user ID, you entered wrong one"));

        String name = principal.getName();

        User existingEngineer = userRepository.findByEmailAndRole(name , User.Role.ENGINEER);

        Bill bill = engineerSerivce.generateBill(dto , user , existingEngineer);

        return ResponseEntity.ok().body(new CommonResponseDTO("bill created for ="+ bill.getUser()));
    }

    @PostMapping("/notification")
    public ResponseEntity<CommonResponseDTO> generatePersonalNotification(@RequestBody UserPersonalNotificationDTO dto,Principal principal)
    {
        String name = principal.getName();
        //engineer who create this message
        User existingEngineer = userRepository.findByEmailAndRole(name ,User.Role.ENGINEER);

        //user to which this message is created
        User user= userRepository.findById(dto.getUserId())
                .orElseThrow(()->new UsernameNotFoundException("please verify user ID that you enter"));

        Notification notification = engineerSerivce.generateNotification(dto , user , existingEngineer);

        return ResponseEntity.ok(new CommonResponseDTO("Personal Notification created for "+ user.getName()));

    }
}
