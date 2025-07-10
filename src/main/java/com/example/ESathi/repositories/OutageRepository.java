package com.example.ESathi.repositories;

import com.example.ESathi.model.Outage;
import com.example.ESathi.model.Stations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutageRepository extends JpaRepository<Outage ,Long> {

    List<Outage> findByStations(Stations station);
}
