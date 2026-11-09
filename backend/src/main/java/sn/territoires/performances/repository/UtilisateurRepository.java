package sn.territoires.performances.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.territoires.performances.entity.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
