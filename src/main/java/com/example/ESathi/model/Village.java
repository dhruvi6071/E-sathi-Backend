package com.example.ESathi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Village {

    @Id
    private Long villageId;

    private String name;

    private String pincode;

    private String taluka;
    private String Disctrit;

    @ManyToOne
    @JoinColumn(name = "sationID")
    private Stations stations;
}
