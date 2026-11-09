package sn.territoires.performances.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.territoires.performances.entity.DomaineCompetence;

public interface DomaineRepository extends JpaRepository<DomaineCompetence, Integer> {
}
