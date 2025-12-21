package com.example.ESathi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;   // the user to whom this notification is shown

    @ManyToOne
    @JoinColumn(name = "created_by_engineer_id", nullable = false)
    private User createdBy;   // the engineer who created this notification

    private String title;

    private String message;

    @Enumerated(EnumType.STRING)
    private Type type;

    private LocalDateTime createdAt;

    private LocalDateTime expireAt;  // after this date, don't show on dashboard

    public enum Type {
        WARNING,
        INFO,
        BILL_DUE,
        BILL ;

        public static Type fromString(String type) {
            if (type == null) return null;
            try {
                return Notification.Type.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null; // or throw custom exception
            }
        }
    }
}
