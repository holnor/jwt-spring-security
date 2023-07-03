package hu.holnor.app.repository;

import hu.holnor.app.config.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
