package com.example.ESathi.repositories;

import com.example.ESathi.model.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;

@Repository
public interface VillageRepository extends JpaRepository<Village , Long>
{
    Optional<Village> findByPincode(String pincode);
}
