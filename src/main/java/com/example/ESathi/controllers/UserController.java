package com.example.ESathi.controllers;

import com.example.ESathi.DTO.UserNeedDTO.AreaHomeWarningDTO;
import com.example.ESathi.DTO.UserNeedDTO.HomeResponseDTO;
import com.example.ESathi.DTO.UserNeedDTO.PendingBillDTO;
import com.example.ESathi.DTO.UserNeedDTO.PersonalNofiticationDTO;
import com.example.ESathi.Serivces.UserService;
import com.example.ESathi.Serivces.wedherSevices.GeminiService;
import com.example.ESathi.Serivces.wedherSevices.WeatherService;
import com.example.ESathi.model.Bill;
import com.example.ESathi.model.User;
import com.example.ESathi.model.Village;
import com.example.ESathi.repositories.BillRepository;
import com.example.ESathi.repositories.UserRepository;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private  final WeatherService weatherService;
    private final GeminiService geminiService;
    private final UserService userService;
    private final BillRepository billRepository;

    public UserController(UserRepository userRepository,
                          WeatherService weatherService,
                          GeminiService geminiService,
                          UserService userService,
                          BillRepository billRepository) {
        this.userRepository = userRepository;
        this.weatherService=weatherService;
        this.geminiService=geminiService;
        this.userService = userService;
        this.billRepository = billRepository;
    }


    @GetMapping("/home")
    public ResponseEntity<HomeResponseDTO> getAllHomeDetail(Principal principal)
    {

//        //Weadher based prediction
        String username= principal.getName();

        User user= userRepository.findByEmail(username)
                .orElseThrow(()->new UsernameNotFoundException("user for this email is not valid"));

        Village village= user.getVillage();
        String villageName= village.getName();
        String pincode = village.getPincode();

        JSONObject wetherData = weatherService.getWeatherJson(pincode);
        JSONObject parsed = geminiService.analyzeWeatherWithGemini(wetherData , villageName);

        //fetc Area notification
        List<AreaHomeWarningDTO> areaWarnings = userService.findAreaWarningMesage(user);
        System.out.println(areaWarnings);

        //fetch last pending bill

            List<PendingBillDTO> optionaBill = billRepository.findTopByUserAndStatusOrderByBillingMonthDesc(user, Bill.Status.UNPAID);
            List<PendingBillDTO> pendingBillDTO= null;

            Map<YearMonth,String> pendingMessage = new HashMap<>();

            if(optionaBill !=null)
            {

                pendingBillDTO = optionaBill ;
                optionaBill.forEach( bill -> {
                    pendingMessage.put(bill.getBillingMonth() ,"You have unpaid bill for " ) ;
                });

            }
            else {
                pendingMessage.put(null,"Good! You have no unpaid bills.");
            }


            // fetch personal notification
            // at time we fetch only manual notification but dynamic is till pending

        PersonalNofiticationDTO personalNofiticationDTOS = userService.getAllPersonalNotification(user);


        return ResponseEntity.ok(new HomeResponseDTO(
                    parsed.getString("area"),
                parsed.getString("weather"),
                parsed.getString("risk"),
                areaWarnings,
                personalNofiticationDTOS,
                pendingBillDTO,
                pendingMessage
        ));

//        return ResponseEntity.ok(new HomeResponseDTO(
//                null,
//                null,
//                null,
//                areaWarnings,
//                personalNofiticationDTOS,
//                pendingBillDTO,
//                pendingMessage
//
//        ));

    }
}
