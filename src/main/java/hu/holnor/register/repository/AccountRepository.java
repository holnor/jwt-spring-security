package hu.holnor.register.repository;

import hu.holnor.register.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
