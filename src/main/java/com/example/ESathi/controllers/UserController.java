package com.example.ESathi.controllers;

import com.example.ESathi.DTO.UserNeedDTO.*;
import com.example.ESathi.Serivces.UserService;
import com.example.ESathi.Serivces.wedherSevices.GeminiService;
import com.example.ESathi.Serivces.wedherSevices.WeatherService;
import com.example.ESathi.model.*;
import com.example.ESathi.repositories.BillRepository;
import com.example.ESathi.repositories.PaymentRepository;
import com.example.ESathi.repositories.StationRepository;
import com.example.ESathi.repositories.UserRepository;
import com.example.ESathi.utils.ApiResponse;
import com.example.ESathi.utils.customeExeptions.BaseException;
import com.example.ESathi.utils.customeExeptions.NameNotMatchException;
import com.example.ESathi.utils.customeExeptions.ResourceNotFoundException;
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
import java.time.LocalDate;
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
    private final StationRepository stationRepository ;

    public UserController(UserRepository userRepository,
                          WeatherService weatherService,
                          GeminiService geminiService,
                          UserService userService,
                          BillRepository billRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          PaymentRepository paymentRepository,
                          StationRepository stationRepository) {

        this.userRepository = userRepository;
        this.weatherService=weatherService;
        this.geminiService=geminiService;
        this.userService = userService;
        this.billRepository = billRepository;
        this.passwordEncoder = passwordEncoder;
        this.paymentRepository = paymentRepository;
        this.stationRepository = stationRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @GetMapping("/home")
    public ResponseEntity<HomeResponseDTO> getAllHomeDetail(Principal principal)
    {

        //Weather based prediction
        String username= principal.getName();

        User user= userRepository.findByEmail(username)
                .orElseThrow(()->new ResourceNotFoundException("user for this email is not valid"));
        Village village= user.getVillage();
        Long stationId=user.getVillage().getStations().getSationID();
        String villageName= village.getName();
        String pincode = village.getPincode();

        JSONObject wetherData = weatherService.getWeatherJson(pincode);
        JSONObject parsed = geminiService.analyzeWeatherWithGemini(wetherData , villageName);

        //fetc Area notification
        List<AreaHomeWarningDTO> areaWarnings = userService.findAreaWarningMesage(user);
//        System.out.println(areaWarnings);

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
                         .status(lastMonthBill.getStatus().toString())
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
                        .status(paidLastMonthBill.getStatus().toString())
                        .message("congtrest you have no panding BIll:")
                        .build();
            }


            // Fetch total Unit that user Consumption in last one year
        double oneYearUnitConsumption = userService.oneYearUnitConsuption(user);

            // Fetch Enginner Detail to perticular Are User
        Stations station = stationRepository.findById(stationId).orElseThrow(
                ()-> new NoSuchElementException("Station not fond")
        );
        System.out.println("----- station -----");
        System.out.println(station);
        User engineer = userRepository.findByRoleAndAssignStation(User.Role.ENGINEER, station);
        if (engineer == null) {
            throw new ResourceNotFoundException("No engineer found");
        }

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
                .orElseThrow(()-> new ResourceNotFoundException("There might be some issue please Re-login" + userName));

        return ResponseEntity.ok(user);
    }

    //user can update his profile
    @PutMapping("/account/update")
    public ApiResponse<User> updateAccount(@RequestBody UpdateUserRequestDTO updateUserRequestDTO , Authentication authentication)
    {
        String name = authentication.getName();
        if(name == null)
        {
            throw new BaseException("User not in Context") ;
        }
//        System.out.println(name);

        try {
//            System.out.println(" user name for updation :" + updateUserRequestDTO.getEmail());

            if(!name.equals(updateUserRequestDTO.getEmail()))
            {
                throw new NameNotMatchException("user not match");
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
            throw new NameNotMatchException("Error with Matching user: " + e.getMessage()) ;
        }
    }


    // payment page controller methods
    @GetMapping("/bills/getAllBills")
    public ApiResponse<Page<AllBillResponseDTO>> getAllBill(@RequestParam(defaultValue = "0") int allBillsPage ,
                                                             @RequestParam(defaultValue = "3") int allBillsSize,
                                                             Principal principal)
    {
        String name = principal.getName() ;
        if(name == null)
        {
            throw new BaseException("User not in Context") ;
        }

        User user = userRepository.findByEmailAndRole(name , User.Role.USER);
        System.out.println("--- user --");
        System.out.println(user);
        if(user == null)
        {
            throw new ResourceNotFoundException("User not find in Data :" + name) ;
        }

        // Pageable for pending and paid separately
        Pageable allBillsPageable = PageRequest.of(allBillsPage, allBillsSize, Sort.by("issueDate").descending());


        //Fetch all bilsl either pais or not
        Page<Bill> allBillResponses= billRepository.findByUser(user , allBillsPageable);

        Page<AllBillResponseDTO> allBillResponseDTO = allBillResponses.map(
                bill->
                {
                    if ( bill.getStatus() == Bill.Status.PAID)
                    {
                        Optional<Payments> paymentOptional = paymentRepository.findByUserAndBill(user, bill);

                        return new AllBillResponseDTO(
                                bill.getBillId(),
                                bill.getUser().getName(),
                                bill.getIssueDate().toLocalDate(),
                                bill.getBillingMonth(),
                                paymentOptional.map(Payments::getPaidDate)
                                        .map(LocalDateTime::toLocalDate)
                                        .orElse(null), // fallback if missing
                                null,
                                bill.getAmountDue(),
                                bill.getUnitConsume(),
                                bill.getStatus()
                        );
                    }
                    else
                    {
                       return new AllBillResponseDTO(
                                bill.getBillId(),
                                bill.getUser().getName(),
                                bill.getIssueDate().toLocalDate(),
                                bill.getBillingMonth(),
                               null ,
                                bill.getDueDate().toLocalDate(),
                                bill.getAmountDue(),
                                bill.getUnitConsume(),
                                bill.getStatus()
                       );
                    }
                }
        );

        return ApiResponse.success(allBillResponseDTO);
    }

    //get Peding bills for payment Page
    @GetMapping("/bills/unpaidBills")
    public ApiResponse<Page<PendingBillsResponseDTO>> getAllPendingBills(@RequestParam(defaultValue = "0") int pendingPage ,
                                                                   @RequestParam(defaultValue = "3") int pendingSize,
                                                                   Principal principal)
    {
        String name = principal.getName();
        if(name == null)
        {
            throw new BaseException("User not in Context") ;
        }
        User user = userRepository.findByEmailAndRole(name , User.Role.USER);
        if(user == null)
        {
            throw new ResourceNotFoundException("User not find in Data :" + name) ;
        }


        //pageable for pending bills
        Pageable pendingPageable = PageRequest.of(pendingPage , pendingSize , Sort.by("issueDate").descending());

        //Fetch Pending bill
        Page<Bill> pendingBillsPage = billRepository.findByUserAndStatus(user , Bill.Status.UNPAID , pendingPageable);

        //set pending bill as per our requirement
        Page<PendingBillsResponseDTO> pendingDTOPage = pendingBillsPage.map(bill ->
                new PendingBillsResponseDTO(
                        bill.getBillId(),
                        bill.getUser().getName(),
                        bill.getIssueDate().toLocalDate(),
                        bill.getBillingMonth(),
                        bill.getDueDate().toLocalDate(),
                        bill.getAmountDue(),
                        bill.getUnitConsume(),
                        bill.getStatus())
        );

        return ApiResponse.success(pendingDTOPage);
    }

    //get Paid bills for payment Page
    @GetMapping("/bills/paidBills")
    public ApiResponse<Page<PaidBillResponseDTO>> getAllPaidBills(@RequestParam(defaultValue = "0") int paidPage ,
                                                                   @RequestParam(defaultValue = "3") int paidSize,
                                                                   Principal principal)
    {
        String name = principal.getName();
        if(name == null)
        {
            throw new BaseException("User not in Context") ;
        }
        User user = userRepository.findByEmailAndRole(name , User.Role.USER);
        if(user == null)
        {
            throw new ResourceNotFoundException("User not find in Data :" + name) ;
        }

        //pageable for pending bills
        Pageable paidPageable = PageRequest.of(paidPage , paidSize , Sort.by("issueDate").descending());

        //Fetch Paid bill
        Page<Bill> paidBillsPage = billRepository.findByUserAndStatus(user , Bill.Status.PAID, paidPageable);

        //Set Paid bill detail as per our requirement
        Page<PaidBillResponseDTO> paidBillsDTOPage = paidBillsPage.map(bill -> {
            Optional<Payments> paymentOptional = paymentRepository.findByUserAndBill(user, bill);

            return new PaidBillResponseDTO(
                    bill.getBillId(),
                    bill.getUser().getName(),
                    bill.getIssueDate().toLocalDate(),
                    bill.getBillingMonth(),
                    paymentOptional.map(Payments::getPaidDate)
                            .map(LocalDateTime::toLocalDate)
                            .orElse(null), // fallback if missing
                    bill.getAmountDue(),
                    bill.getUnitConsume(),
                    Bill.Status.PAID
            );
        });

        return ApiResponse.success(paidBillsDTOPage);
    }





    //uasage page for user UnitConsuption and Average Consuption

    @GetMapping("/Usages")
    public ApiResponse<UsagePageResponseDTO> getUsagePageDetail(Principal principal)
    {
        String name = principal.getName();
        if(name == null)
        {
            throw new BaseException("User not in Context") ;
        }

        User user = userRepository.findByEmailAndRole(name, User.Role.USER);
        if(user == null)
        {
            throw new ResourceNotFoundException("User not find in Data :" + name) ;
        }

        //get billing date with unit for graph analysi
        Map<LocalDate, Double> lastOneYearBills = userService.findUnitAndDateOfLastOneYear(user);

        //get AVG of lat one year
        List<Double> oneYearUnitAVGAndNumber = userService.oneyearUnitAverage(user);
        double oneYearUnitAVG = oneYearUnitAVGAndNumber.get(1);

        //get last month Unit Consumption and status
        Bill lastMonthBill = billRepository.findTopByUserOrderByIssueDateDesc(user)
                .orElseThrow(()-> new UsernameNotFoundException("bill not found for last bill"));

        double lastMonthUnit = lastMonthBill.getUnitConsume();
        Bill.Status status = lastMonthBill.getStatus();

        //gemini prediction for unit consumptions
        String geminiPrediction = userService.getGeminiPrediction(user);

        UsagePageResponseDTO usagePageResponseDTO = UsagePageResponseDTO.builder()
                .unitWithDate(lastOneYearBills)
                .oneYearUnitAVG(oneYearUnitAVG)
                .lastMonthUnitConsume(lastMonthUnit)
                .lastMonthBillStatus(status.toString())
                .geminiPrediction(geminiPrediction)
                .build();

        return ApiResponse.success(usagePageResponseDTO);

    }
}
