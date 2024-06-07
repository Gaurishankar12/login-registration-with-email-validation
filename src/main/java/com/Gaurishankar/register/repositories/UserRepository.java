package com.Gaurishankar.register.repositories;

import com.Gaurishankar.register.models.ApplicationUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<ApplicationUser, Integer> {

  // SDP
  Optional<ApplicationUser> findByEmail(String email);
}
