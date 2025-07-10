package com.example.ESathi.authentication.repository;

import com.example.ESathi.authentication.modles.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token,String> {

    List<Token> findByUserName(String email);

    Optional<Token> findByToken(String token);;
}
