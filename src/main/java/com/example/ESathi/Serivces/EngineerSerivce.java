package com.example.ESathi.Serivces;

import com.example.ESathi.DTO.EngineerNeddDTO.AreaWarringRequestDTO;
import com.example.ESathi.DTO.EngineerNeddDTO.BillGenerateRequestDTO;
import com.example.ESathi.DTO.EngineerNeddDTO.UserPersonalNotificationDTO;
import com.example.ESathi.model.*;
import com.example.ESathi.repositories.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

@Service
public class EngineerSerivce {

    private  final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final OutageRepository outageRepository;
    private final BillRepository billRepository;
    private final NotificationRepository notificationRepository;


    public EngineerSerivce(StationRepository stationRepository,
                           UserRepository userRepository,
                           OutageRepository outageRepository,
                           BillRepository billRepository,
                           NotificationRepository notificationRepository) {

        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
        this.outageRepository = outageRepository;
        this.billRepository = billRepository;
        this.notificationRepository = notificationRepository;
    }


    public Outage generateNewOutage(AreaWarringRequestDTO dto , String name)
    {
        Stations station= stationRepository.findByName(dto.getStationName());

        User existeEngineer = userRepository.findByEmailAndRole(name , User.Role.ENGINEER);

        Outage outage = Outage.builder()
                .expectedEndTime(dto.getSolveDate())
                .reason(dto.getMessage())
                .reportedBy(existeEngineer)
                .status(Outage.Status.UNPLANNED)
                .resolvedTime(null)
                .startTime(dto.getStartDate())
                .stations(station)
                .build();

        outageRepository.save(outage);

        return outage ;
    }

    public Bill generateBill(BillGenerateRequestDTO dto , User user , User engineer)
    {
        Date now = new Date(); // cuurent date

        Bill bill= Bill.builder()
                .createBy(engineer)
                .issueDate(now.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .status(Bill.Status.UNPAID)
                .unitConsume(dto.getUnitsUsed())
                .billingMonth(YearMonth.now())
                .amountDue(dto.getUnitsUsed()*5.50)
                .dueDate(LocalDateTime.now().plusMonths(3))
                .user(user)
                .build();

        billRepository.save(bill);

        return bill ;

    }

    public Notification generateNotification(UserPersonalNotificationDTO dto, User user, User existingEngineer)
    {

        Notification notification = Notification.builder()
                .title(dto.getTitle())
                .type(Notification.Type.valueOf(dto.getType()))
                .message(dto.getMessage())
                .createdBy(existingEngineer)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expireAt(LocalDateTime.now().plusDays(2))
                .build();

        notificationRepository.save(notification);
        return notification ;

    }
}
