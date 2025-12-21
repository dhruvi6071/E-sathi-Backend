package com.example.ESathi.Serivces;

import com.example.ESathi.DTO.UserNeedDTO.AreaHomeWarningDTO;
import com.example.ESathi.DTO.UserNeedDTO.PersonalNofiticationDTO;
import com.example.ESathi.Serivces.wedherSevices.GeminiService;
import com.example.ESathi.model.*;
import com.example.ESathi.repositories.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final OutageRepository outageRepository;
    private final NotificationRepository notificationRepository;
    private final BillRepository billRepository;
    private  final GeminiService geminiService;

    public UserService(UserRepository userRepository,
                       StationRepository stationRepository,
                       OutageRepository outageRepository,
                       NotificationRepository notificationRepository,
                       BillRepository billRepository,
                       GeminiService geminiService) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.outageRepository = outageRepository;
        this.notificationRepository = notificationRepository;
        this.billRepository = billRepository;
        this.geminiService = geminiService;
    }


    public List<AreaHomeWarningDTO> findAreaWarningMesage(User user)
    {
        Stations stations = user.getAssignStation();
        List<Outage> outages = outageRepository.findByStations(stations);


        List<AreaHomeWarningDTO>  outage=  outages.stream()
                .map(
                        o-> new AreaHomeWarningDTO(o.getReason() , o.getStatus().toString())
                ).toList();


        return outage ;

    }

    public PersonalNofiticationDTO getAllPersonalNotification(User user)
    {

        //generate personal message based on bill
        List<Bill> bills = billRepository.findByUserAndStatus(user, Bill.Status.UNPAID);
        String billMessage=null ;
        if(!bills.isEmpty())
        {
            int unpaidBill = bills.size();
            Bill lastBill = bills.get(unpaidBill-1);
            LocalDateTime now = LocalDateTime.now();

            if(lastBill.getDueDate().isBefore(now))
            {

                billMessage = "Your last bill is overdue. Please pay it as soon as possible. "
                        +lastBill.getDueDate().getDayOfMonth()+" " + lastBill.getDueDate().getMonth()+" " + lastBill.getDueDate().getYear();
                if(unpaidBill > 1)
                {
                    billMessage += "aslo check your are more pending bill, total " + unpaidBill;
                }
            }
            else {
                billMessage = "Your bill is Pending Due date: " + lastBill.getDueDate();
            }
        }


        //generate personal message based on unitConsumed;
        List<Bill> bills1 = billRepository.findByUser(user);
        Bill lastBill = bills1.get(0);
        List<Double> totalUnit = bills1.stream()
                .map(
                        Bill::getUnitConsume
                ).toList();
        String unitMessage= null;
        //average finding logic
        double avg = totalUnit.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        //last month unit for comparison
        double lastUnit = lastBill.getUnitConsume();
        if (lastUnit > avg + 10) { // Threshold (e.g., 10 units more than avg)
            unitMessage = " Your recent electricity usage is higher than usual. Consider checking for heavy appliances."+
                    lastBill.getBillingMonth() + "More Unit Consume : " + Math.abs(lastUnit - avg);

        }else {
            unitMessage = "Your electricity usage is normal. Avg: " + avg + ", This month: " + lastUnit;
        }

        // user personal notification issue by engineer ;
        List<Notification> notifications = notificationRepository.findByUser(user);
        List<String> presonalMessage
                = notifications.stream()
                .map(
                        Notification::getMessage
                ).toList();

        PersonalNofiticationDTO personalNofiticationDTOS = PersonalNofiticationDTO.builder()
                .personalmessage(presonalMessage)
                .billMessage(billMessage)
                .unitMessage(unitMessage)
                .build();

        return personalNofiticationDTOS;
    }

    // one year Unite Consuption

    public double oneYearUnitConsuption(User user)
    {

        //get one year prious date
        LocalDateTime fromDate = LocalDateTime.now().minusYears(1);

        double totalOneYearUnitConsuption = billRepository.getTotalUnitConsumptionInLastYear(user , fromDate);

        return totalOneYearUnitConsuption ;
    }

    //get all bills of last year and then retunr only unit and bill date
    public Map<LocalDate , Double> findUnitAndDateOfLastOneYear(User user)
    {
        LocalDateTime fromDate = LocalDateTime.now().minusYears(1);
        List<Bill> bills = billRepository.findBillsFromLastOneYear(user.getUserID(), fromDate);

        Map<LocalDate , Double> unitAndDate = bills.stream()
                .collect(Collectors.toMap(
                        bill -> bill.getIssueDate().toLocalDate() ,
                        Bill::getUnitConsume
                ));

        return unitAndDate ;
    }

    // generate message for user to decribe how much unit it cosume last month based on average of total
    public String generateLastMonthUnitAvgMessage(User user)
    {
        List<Double> averageUnitAndTotalBill = oneyearUnitAverage(user);

        double averageYearlyUnit = averageUnitAndTotalBill.get(1);


       Bill lastMonthBill = billRepository.findTopByUserOrderByIssueDateDesc(user)
               .orElseThrow(()-> new UsernameNotFoundException("bill not found for last bill"));

       double lastMonthUnit = lastMonthBill.getUnitConsume();


       // Create suggestion message
        String message;
        if (lastMonthUnit > averageYearlyUnit) {
            message = String.format(
                    "Warning: Your last month's usage (%.2f units) is higher than your 1-year average (%.2f units). Please consider reducing electricity consumption.",
                    lastMonthUnit, averageYearlyUnit
            );
        } else if (lastMonthUnit < averageYearlyUnit) {
            message = String.format(
                    "Good job! Your last month's usage (%.2f units) is lower than your 1-year average (%.2f units). Keep saving energy!",
                    lastMonthUnit, averageYearlyUnit
            );
        } else {
            message = String.format(
                    "️ Your last month's usage (%.2f units) is equal to your 1-year average. Try to lower it to save more.",
                    lastMonthUnit
            );
        }

        return message ;
    }

    //gemini prediction for Usages page
    public String getGeminiPrediction(User user)
    {
        List<Double> averageUnitAndTotalBill = oneyearUnitAverage(user);

        double averageUnitLastOneYear = averageUnitAndTotalBill.get(1);
        double totalBillConsiderForAVG =  averageUnitAndTotalBill.get(0);

        Bill lastMonthBill = billRepository.findTopByUserOrderByIssueDateDesc(user)
                .orElseThrow(()-> new UsernameNotFoundException("bill not found for last bill"));

        double lastMonthUnit = lastMonthBill.getUnitConsume();
        Bill.Status status = lastMonthBill.getStatus();

        String message = geminiService.getConsumptionAdvice(averageUnitLastOneYear , totalBillConsiderForAVG,
                                                            lastMonthUnit , status.toString());
        return message ;
    }

    //genearate one year unit average
    public List<Double> oneyearUnitAverage(User user)
    {
        LocalDateTime fromDate = LocalDateTime.now().minusYears(1);
        List<Bill> bills = billRepository.findBillsFromLastOneYear(user.getUserID(), fromDate);

        double unitConsume = bills.stream()
                .mapToDouble(Bill::getUnitConsume)
                .sum();

        int totalBills = bills.size();

        double averageYearlyUnit = 0 ;
        if(totalBills != 0 && unitConsume != 0)
        {
            averageYearlyUnit = unitConsume / totalBills;
        }

        List<Double> list = new ArrayList();

        list.add((double)totalBills);
        list.add(averageYearlyUnit);
        return  list ;
    }


}
