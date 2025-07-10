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
public class Outage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long outageID;

    @ManyToOne
    @JoinColumn(name = "stationID")
    private Stations stations;

    @ManyToOne
    @JoinColumn(name = "userID")
    private User reportedBy;

    private String reason ;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;
    private LocalDateTime resolvedTime;

    public enum Status {
        PLANNED,
        UNPLANNED,
        ONGOING,
        RESOLVED
    }
}
