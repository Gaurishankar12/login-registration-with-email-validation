package com.Gaurishankar.register.services;


import com.Gaurishankar.register.dto.RegistrationDto;
import com.Gaurishankar.register.models.ApplicationUser;
import com.Gaurishankar.register.models.Token;
import com.Gaurishankar.register.models.UserRole;
import com.Gaurishankar.register.repositories.TokenRepository;
import com.Gaurishankar.register.repositories.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {

  private static final String CONFIRMATION_URL = "http://localhost:8080/api/v1/authentication/confirm?token=%s";

  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  @Transactional
  public String register(RegistrationDto registrationDto) {
    // check if the user already exists
    boolean userExists = repository.findByEmail(registrationDto.getEmail()).isPresent();
    if (userExists) {
      throw new IllegalStateException("A user already exists with the same email");
    }
    // Encode the password
    String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());

    // transform - map the RegistrationDto to ApplicationDto
    ApplicationUser applicationUser = ApplicationUser.builder()
        .firstname(registrationDto.getFirstname())
        .lastname(registrationDto.getLastname())
        .email(registrationDto.getEmail())
        .password(encodedPassword)
        .role(UserRole.ROLE_USER)
        .build();

    // Save the user
    ApplicationUser savedUser = repository.save(applicationUser);

    // Generate a token
    String generatedToken = UUID.randomUUID().toString();
    Token token = Token.builder()
        .token(generatedToken)
        .createdAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusMinutes(10))
        .user(savedUser)
        .build();
    tokenRepository.save(token);

    // Send the confirmation email
    try {
      emailService.send(
          registrationDto.getEmail(),
          registrationDto.getFirstname(),
          null,
          String.format(CONFIRMATION_URL, generatedToken)
      );
    } catch (MessagingException e) {
      e.printStackTrace();
    }

    // return success message
    return generatedToken;
  }

  public String confirm(String token) {
    // get the token
    Token savedToken = tokenRepository.findByToken(token)
        .orElseThrow(() -> new IllegalStateException("Token not found"));
    if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
      // Generate a token
      String generatedToken = UUID.randomUUID().toString();
      Token newToken = Token.builder()
          .token(generatedToken)
          .createdAt(LocalDateTime.now())
          .expiresAt(LocalDateTime.now().plusMinutes(10))
          .user(savedToken.getUser())
          .build();
      tokenRepository.save(newToken);
      try {
        emailService.send(
            savedToken.getUser().getEmail(),
            savedToken.getUser().getFirstname(),
            null,
            String.format(CONFIRMATION_URL, generatedToken)
        );
      } catch (MessagingException e) {
        e.printStackTrace();
      }
      return "Token expired, a new token has been sent to your email";
    }

    ApplicationUser user = repository.findById(savedToken.getUser().getId())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    user.setEnabled(true);
    repository.save(user);

    savedToken.setValidatedAt(LocalDateTime.now());
    tokenRepository.save(savedToken);
    return "<h1>Your account hase been successfully activated</h1>";
  }
}