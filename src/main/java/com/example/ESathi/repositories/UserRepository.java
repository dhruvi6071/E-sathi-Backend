package com.example.ESathi.repositories;

import com.example.ESathi.model.Stations;
import com.example.ESathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.management.relation.Role;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long>
{

    Optional<User> findByEmail(String email);
    User findByEmailAndRole(String email , User.Role role);

    User findByRoleAndAssignStation(User.Role role, Stations station);


}


