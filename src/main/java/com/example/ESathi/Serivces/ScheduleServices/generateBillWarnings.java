package com.example.ESathi.Serivces.ScheduleServices;

import com.example.ESathi.model.Bill;
import com.example.ESathi.model.Notification;
import com.example.ESathi.model.User;
import com.example.ESathi.repositories.BillRepository;
import com.example.ESathi.repositories.NotificationRepository;
import com.example.ESathi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class generateBillWarnings {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 12 * * ?") // Every day at 12:00 PM
    public void generateBillWarnings() {
        List<User> users = userRepository.findAll();


        for (User user : users) {

            // Clean old BILL notifications first
            List<Notification> notifications = notificationRepository.findByUserAndType(user, "BILL");
            notificationRepository.deleteAll(notifications); // cleaner than looping


            List<Bill> pendingBills = billRepository.findByUserAndStatus(user, Bill.Status.UNPAID);

            if (!pendingBills.isEmpty()) {
                int monthsPending = pendingBills.size();
                double totalAmount = pendingBills.stream()
                        .mapToDouble(Bill::getAmountDue)
                        .sum();

                String message = "⚠ You have " + monthsPending + " month(s) of pending bills. "
                        + "Total due: ₹" + totalAmount;

                Notification notification = new Notification();
                notification.setMessage(message);
                notification.setUser(user);
                notification.setType(Notification.Type.valueOf("BILL"));
                notification.setCreatedAt(LocalDateTime.now());

                notificationRepository.save(notification);
            }
        }
    }

}
