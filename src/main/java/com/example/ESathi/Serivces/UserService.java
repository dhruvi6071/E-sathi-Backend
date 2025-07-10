package com.example.ESathi.Serivces;

import com.example.ESathi.DTO.UserNeedDTO.AreaHomeWarningDTO;
import com.example.ESathi.DTO.UserNeedDTO.PersonalNofiticationDTO;
import com.example.ESathi.model.*;
import com.example.ESathi.repositories.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final OutageRepository outageRepository;
    private final NotificationRepository notificationRepository;
    private final BillRepository billRepository;

    public UserService(UserRepository userRepository,
                       StationRepository stationRepository,
                       OutageRepository outageRepository,
                       NotificationRepository notificationRepository,
                       BillRepository billRepository) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.outageRepository = outageRepository;
        this.notificationRepository = notificationRepository;
        this.billRepository = billRepository;
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
}
