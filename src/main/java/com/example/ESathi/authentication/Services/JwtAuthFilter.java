package com.example.ESathi.authentication.Services;

import com.example.ESathi.authentication.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenSerivce jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    public JwtAuthFilter(JwtTokenSerivce jwtService,
                         CustomUserDetailsService userDetailsService,
                         TokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String email = jwtService.extractUsername(jwt);
        System.out.println("Incoming Auth Header: " + authHeader);
        System.out.println("Token extracted email: " + email);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // ✅ Check token is valid & not revoked
            boolean isTokenValid = jwtService.isTokenValid(jwt, userDetails)
                    && tokenRepository.findByToken(jwt)
                    .filter(t -> !t.isRevoked())
                    .isPresent();

            if (isTokenValid) {
                for(GrantedAuthority auth: userDetails.getAuthorities()){
                    System.out.println("x"+auth.getAuthority());
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                System.out.println( "context authToken ="+authToken);
                SecurityContextHolder.getContext().setAuthentication(authToken);

                System.out.println("Authenticated as: " + email);
            } else {
                System.out.println("JWT is invalid or revoked.");
            }
        }

        filterChain.doFilter(request, response);
    }
}

