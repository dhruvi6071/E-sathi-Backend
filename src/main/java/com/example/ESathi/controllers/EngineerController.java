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
import com.example.ESathi.repositories.UserRepository;
import com.example.ESathi.utils.customeExeptions.BaseException;
import com.example.ESathi.utils.customeExeptions.ResourceNotFoundException;
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

    public EngineerController(StationRepository stationRepository,
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
        // find user to which fill assigned
        User user= userRepository.findById(dto.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("please check user ID, you entered wrong one" + dto.getUserId()));

        //this is to get Existing use which is engineer
        String name = principal.getName();
        if(name == null)
        {
            throw new BaseException("User not in Context") ;
        }

        // find detail of existing engineer
        User existingEngineer = userRepository.findByEmailAndRole(name , User.Role.ENGINEER);
        if(user == null)
        {
            throw new ResourceNotFoundException("User not find in Data :" + name) ;
        }

        // generate bill for user
        Bill bill = engineerSerivce.generateBill(dto , user , existingEngineer);

        return ResponseEntity.ok().body(new CommonResponseDTO("bill created for ="+ bill.getUser()));
    }

    @PostMapping("/notification")
    public ResponseEntity<CommonResponseDTO> generatePersonalNotification(@RequestBody UserPersonalNotificationDTO dto,Principal principal)
    {
        //Existing engineer
        String name = principal.getName();
        if(name == null)
        {
            throw new BaseException("User not in Context") ;
        }

        //engineer who create this message
        User existingEngineer = userRepository.findByEmailAndRole(name ,User.Role.ENGINEER);
        if(name == null)
        {
            throw new BaseException("User not in Context") ;
        }

        //user to which this message is created
        User user= userRepository.findById(dto.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("please verify user ID that you enter" + dto.getUserId()));

        Notification notification = engineerSerivce.generateNotification(dto , user , existingEngineer);

        return ResponseEntity.ok(new CommonResponseDTO("Personal Notification created for "+ user.getName()));

    }
}
