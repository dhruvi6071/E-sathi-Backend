package com.example.ESathi.repositories;

import com.example.ESathi.model.Stations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<Stations , Long> {

        Stations findByName(String name);


}
