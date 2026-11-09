package sn.territoires.performances.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.territoires.performances.entity.Indicateur;

public interface IndicateurRepository extends JpaRepository<Indicateur, Long> {
}
