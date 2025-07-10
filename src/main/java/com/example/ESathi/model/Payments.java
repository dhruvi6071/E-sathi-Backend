package com.example.ESathi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long paymentId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
    private Double amountPaid;
    private LocalDateTime paidDate;
    private Methods method;
    private String transationalRef ;

    public enum Methods {
        UPI,
        CARD,
        NET_BANKING
    }

}
