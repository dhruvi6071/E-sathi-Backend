package com.example.ESathi.repositories;

import com.example.ESathi.model.Bill;
import com.example.ESathi.model.Payments;
import com.example.ESathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payments , Long> {

    Optional<Payments> findByUserAndBill(User user, Bill bill);
}
