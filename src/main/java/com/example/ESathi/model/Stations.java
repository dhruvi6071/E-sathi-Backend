package com.example.ESathi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stations {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long sationID;

    private String name ;

    @ManyToOne
    @JoinColumn(name = "areaID")
    private UserArea userArea;

    @ManyToOne
    @JoinColumn(name = "villageID")
    private Village village;
}
