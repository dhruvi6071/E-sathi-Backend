package com.example.ESathi.authentication.Services;

import com.example.ESathi.authentication.modles.Token;
import com.example.ESathi.authentication.repository.TokenRepository;
import com.example.ESathi.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtTokenSerivce {

    @Autowired
    private TokenRepository tokenRepository;

    private final String SECRET_KEY = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";

    public String generateAccessToken(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        List<Token> tokens =   tokenRepository.findByUserName(user.getEmail());
        tokens.forEach(t->
        {
            if(t.getTokenType().equals("ACCESS"))
            {
                tokenRepository.delete(t);
            }
        });


        Date now = new Date(); //return current date
        Date expiryDate = new Date(now.getTime()+ 1000 * 60 * 60 * 10);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authority", "ROLE_" + user.getRole().name());

        String token= Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        Token token1 = new Token();
        token1.setToken(token);
        token1.setTokenType("ACCESS");
        token1.setRevoked(false);
        token1.setExpiryDate(expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        token1.setUserName(user.getEmail());
        tokenRepository.save(token1);

        return token;
    }

    public String generateRefreshToken(Authentication authentication)
    {

        User user = (User) authentication.getPrincipal();


        Date now = new Date(); //return current date
        Date expiryDate = new Date(now.getTime()+ 7 * 24 * 60 * 60 * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authority", "ROLE_" + user.getRole().name());

        String token= Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        List<Token> existingToken =tokenRepository.findByUserName(user.getEmail());
        existingToken.stream()
                .filter(t->"REFRESH".equals(t.getTokenType())&& !t.isRevoked())
                .forEach(t->{
                        t.setRevoked(true);
                        tokenRepository.save(t);
                });

        Token token1 = new Token();
        token1.setToken(token);
        token1.setTokenType("REFRESH");
        token1.setRevoked(false);
        token1.setExpiryDate(expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        token1.setUserName(user.getEmail());
        tokenRepository.save(token1);

        return token;
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
