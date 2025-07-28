package com.example.ESathi.repositories;

import com.example.ESathi.DTO.UserNeedDTO.AllBillResponseDTO;
import com.example.ESathi.DTO.UserNeedDTO.LastPendingBillDTO;
import com.example.ESathi.model.Bill;
import com.example.ESathi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill,Long> {

    Page<Bill> findByUserAndStatus(User user , Bill.Status status, Pageable pageable);
    List<Bill> findByUserAndStatus(User user , Bill.Status status);

    //retunr first bill based on status like
    Bill findFirstByUserAndStatusOrderByBillingMonthDesc(User user, Bill.Status status);

    Optional<Bill> findTopByUserOrderByIssueDateDesc(User user);

    //find user unite Consuption of one year
    @Query("SELECT COALESCE(SUM(b.unitConsume), 0) FROM Bill b " +
            "WHERE b.user = :user AND b.issueDate >= :fromDate")
    double getTotalUnitConsumptionInLastYear(@Param("user") User user,
                                           @Param("fromDate") LocalDateTime fromDate);

    //get whole last year bills for usage page
    @Query("SELECT b FROM Bill b WHERE b.user.id = :userId AND b.issueDate >= :startDate ORDER BY b.issueDate DESC")
    List<Bill> findBillsFromLastOneYear(@Param("userId") Long userId,
                                        @Param("startDate") LocalDateTime startDate);


    List<Bill> findByUser(User user);

    Page<Bill> findByUser(User user , Pageable pageable);
}
