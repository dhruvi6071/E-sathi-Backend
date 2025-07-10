package com.example.ESathi.repositories;

import com.example.ESathi.model.Notification;
import com.example.ESathi.model.User;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification ,Long> {

    List<Notification> findByUserAndType(User user , String type);
    List<Notification> findByUser(User user);
}
