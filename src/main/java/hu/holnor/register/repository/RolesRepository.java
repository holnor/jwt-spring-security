package hu.holnor.register.repository;

import hu.holnor.register.config.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
