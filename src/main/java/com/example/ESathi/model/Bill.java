package com.example.ESathi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.pl.NIP;

import java.lang.annotation.Documented;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  Long billId;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "engineer_id", nullable = false)
    private User createBy;
    private YearMonth billingMonth;
    private Double amountDue;

    private Double unitConsume;

    private Status status;

    private LocalDateTime issueDate;
    private LocalDateTime dueDate;

    public enum Status{
        PAID,
        UNPAID,
        ABOVEDUE
    }
}
