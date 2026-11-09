package sn.territoires.performances.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.territoires.performances.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByCode(String code);
}
