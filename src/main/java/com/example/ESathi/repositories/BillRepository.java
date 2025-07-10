package com.example.ESathi.repositories;

import com.example.ESathi.DTO.UserNeedDTO.PendingBillDTO;
import com.example.ESathi.model.Bill;
import com.example.ESathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill,Long> {

    List<Bill> findByUserAndStatus(User user , Bill.Status status);

    @Query("SELECT NEW com.example.ESathi.DTO.UserNeedDTO.PendingBillDTO(b.billingMonth, b.amountDue) " +
            "FROM Bill b WHERE b.user = :user AND b.status = :status " +
            "ORDER BY b.billingMonth DESC")
    List<PendingBillDTO> findTopByUserAndStatusOrderByBillingMonthDesc(
            @Param("user") User user,
            @Param("status") Bill.Status status
    );

    List<Bill> findByUser(User user);
}
