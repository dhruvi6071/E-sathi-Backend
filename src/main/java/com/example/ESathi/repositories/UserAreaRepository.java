package com.example.ESathi.repositories;

import com.example.ESathi.model.UserArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Repository
public interface UserAreaRepository extends JpaRepository<UserArea , Long> {

    Optional<UserArea> findByName(String name);

}
