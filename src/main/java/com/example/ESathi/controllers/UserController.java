package com.example.ESathi.controllers;

import com.example.ESathi.DTO.UserNeedDTO.*;
import com.example.ESathi.Serivces.UserService;
import com.example.ESathi.Serivces.wedherSevices.GeminiService;
import com.example.ESathi.Serivces.wedherSevices.WeatherService;
import com.example.ESathi.model.*;
import com.example.ESathi.repositories.BillRepository;
import com.example.ESathi.repositories.PaymentRepository;
import com.example.ESathi.repositories.UserRepository;
import com.example.ESathi.utils.ApiResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserRepository userRepository;
    private  final WeatherService weatherService;
    private final GeminiService geminiService;
    private final UserService userService;
    private final BillRepository billRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PaymentRepository paymentRepository;

    public UserController(UserRepository userRepository,
                          WeatherService weatherService,
                          GeminiService geminiService,
                          UserService userService,
                          BillRepository billRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          PaymentRepository paymentRepository) {

        this.userRepository = userRepository;
        this.weatherService=weatherService;
        this.geminiService=geminiService;
        this.userService = userService;
        this.billRepository = billRepository;
        this.passwordEncoder = passwordEncoder;
        this.paymentRepository = paymentRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


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

        Bill lastMonthBill = billRepository.findFirstByUserAndStatusOrderByBillingMonthDesc(user, Bill.Status.UNPAID);

        LastPendingBillDTO lastPendingBillDTO ;
            if(lastMonthBill !=null)
            {
                 lastPendingBillDTO = LastPendingBillDTO.builder()
                        .billingMonth(lastMonthBill.getBillingMonth())
                        .amount(lastMonthBill.getAmountDue())
                        .dueDate(Date.from(lastMonthBill.getDueDate().atZone(ZoneId.systemDefault()).toInstant()))
                        .billdate(Date.from(lastMonthBill.getIssueDate().atZone(ZoneId.systemDefault()).toInstant()))
                        .unitCousume(lastMonthBill.getUnitConsume())
                        .message("Pending Bill information:")
                        .build();
            }
            else {
                Optional<Bill> optionaLastMonthBill = billRepository.findTopByUserOrderByIssueDateDesc(user);
                Bill paidLastMonthBill = optionaLastMonthBill.get();
                lastPendingBillDTO = LastPendingBillDTO.builder()
                        .amount(paidLastMonthBill.getAmountDue())
                        .dueDate(Date.from(paidLastMonthBill.getDueDate().atZone(ZoneId.systemDefault()).toInstant()))
                        .billingMonth(paidLastMonthBill.getBillingMonth())
                        .billdate(Date.from(paidLastMonthBill.getIssueDate().atZone(ZoneId.systemDefault()).toInstant()))
                        .unitCousume(paidLastMonthBill.getUnitConsume())
                        .message("congtrest you have no panding BIll:")
                        .build();
            }


            // Fetch total Unit that user Consumption in last one year
        double oneYearUnitConsumption = userService.oneYearUnitConsuption(user);

            // Fetch Enginner Detail to perticular Are User
        Stations stations = user.getAssignStation();
        User engineer = userRepository.findByRoleAndAssignStation(User.Role.ENGINEER , stations);
        EngineerResponseForHomeDTO engineerResponseForHomeDTO = EngineerResponseForHomeDTO.builder()
                .name(engineer.getName())
                .phone(engineer.getPhone())
                .email(engineer.getEmail())
                .build();

        return ResponseEntity.ok(new HomeResponseDTO(
                    parsed.getString("area"),
                parsed.getString("weather"),
                parsed.getString("risk"),
                areaWarnings,
                oneYearUnitConsumption,
                lastPendingBillDTO,
                engineerResponseForHomeDTO
        ));

//        return ResponseEntity.ok(new HomeResponseDTO(
//                null,
//                null,
//                null,
//                areaWarnings,
//                oneYearUnitConsumption,
//                lastPendingBillDTO,
//               engineerResponseForHomeDTO
//
//        ));

    }

    //user Acount get user detail
    @GetMapping("/account")
    public ResponseEntity<User> getLoginUser(Authentication authentication)
    {
        String userName = authentication.getName();

        //find user that at time login
        User user = userRepository.findByEmail(userName)
                .orElseThrow(()-> new UsernameNotFoundException("There might be some issue please Re-login"));

        return ResponseEntity.ok(user);
    }

    //user can update his profile
    @PutMapping("/update")
    public ApiResponse<User> updateAccount(@RequestBody UpdateUserRequestDTO updateUserRequestDTO , Authentication authentication)
    {
        String name = authentication.getName();
//        System.out.println(name);

        try {
//            System.out.println(" user name for updation :" + updateUserRequestDTO.getEmail());

            if(!name.equals(updateUserRequestDTO.getEmail()))
            {
                throw  new Exception("user not match");
            }

            // get user detial from db
            User user = userRepository.findByEmailAndRole(updateUserRequestDTO.getEmail(), User.Role.USER);


            //Update only if new values are present
            if (updateUserRequestDTO.getName() != null) {
                user.setName(updateUserRequestDTO.getName());
            }

            if (updateUserRequestDTO.getPhone() != null) {
                user.setPhone(updateUserRequestDTO.getPhone());
            }

            if (updateUserRequestDTO.getPassword() != null && !updateUserRequestDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updateUserRequestDTO.getPassword()));
            }

            userRepository.save(user);

            return ApiResponse.success(user);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
           return  ApiResponse.error(e.getMessage() , HttpStatus.BAD_REQUEST);

        }
    }


    // payment page controller methods
    @GetMapping("/getAllBills")
    public ResponseEntity<AllBillResponseDTO> getAllBill(@RequestParam(defaultValue = "0") int pendingPage ,
                                                             @RequestParam(defaultValue = "3") int pendingSize,
                                                             @RequestParam(defaultValue = "0") int paidPage,
                                                             @RequestParam(defaultValue = "5") int paidSize,
                                                             Principal principal)
    {
        String name = principal.getName() ;
        User user = userRepository.findByEmailAndRole(name , User.Role.USER);

        // Pageable for pending and paid separately
        Pageable pendingPageable = PageRequest.of(pendingPage, pendingSize, Sort.by("issueDate").descending());
        Pageable paidPageable = PageRequest.of(paidPage, paidSize, Sort.by("issueDate").descending());


        //Fetch Pending bill
        Page<Bill> pendingBillsPage = billRepository.findByUserAndStatus(user , Bill.Status.UNPAID , pendingPageable);

        //set pending bill as per our requirement
        Page<PendingBillsResponseDTO> pendingDTOPage = pendingBillsPage.map(bill ->
                new PendingBillsResponseDTO(
                        bill.getBillId(), bill.getUser().getName(),
                        bill.getIssueDate(), bill.getDueDate(),
                        bill.getAmountDue(), bill.getUnitConsume(), bill.getStatus())
        );

        //Fetch Paid bill
        Page<Bill> paidBillsPage = billRepository.findByUserAndStatus(user , Bill.Status.PAID, paidPageable);

        //Set Paid bill detail as per our requirement
        Page<PaidBillResponseDTO> paidBillsDTOPage = paidBillsPage.map(bill -> {
            Optional<Payments> paymentOptional = paymentRepository.findByUserAndBill(user, bill);

            return new PaidBillResponseDTO(
                    bill.getBillId(),
                    bill.getUser().getName(),
                    bill.getIssueDate(),
                    paymentOptional.map(Payments::getPaidDate).orElse(null), // fallback if missing
                    bill.getAmountDue(),
                    bill.getUnitConsume(),
                    Bill.Status.PAID
            );
        });


        AllBillResponseDTO allBillResponseDTO = AllBillResponseDTO.builder()
                .pendingBills(pendingDTOPage)
                .paidBill(paidBillsDTOPage)
                .build();

        return ResponseEntity.ok(allBillResponseDTO);
    }


    //uasage page for user UnitConsuption and Average Consuption

    @GetMapping("/Usages")
    public ApiResponse<UsagePageResponseDTO> getUsagePageDetail(Principal principal)
    {
        String name = principal.getName();

        User user = userRepository.findByEmailAndRole(name, User.Role.USER);

        //get billing date with unit for graph analysi
        Map<LocalDateTime , Double> lastOneYearBills = userService.findUnitAndDateOfLastOneYear(user);


        //get message for unsages of unit maunal analysis
        String lastMonthBillMessage = userService.generateLastMonthUnitAvgMessage(user);

        //gemini prediction for unit consumptions
        String geminiPrediction = userService.getGeminiPrediction(user);

        return ApiResponse.success(new UsagePageResponseDTO(lastOneYearBills , lastMonthBillMessage , geminiPrediction));

    }
}
