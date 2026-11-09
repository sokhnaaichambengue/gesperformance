package sn.territoires.performances.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.territoires.performances.entity.Investissement;

public interface InvestissementRepository extends JpaRepository<Investissement, Long> {
    List<Investissement> findByProjetIdOrderByDateOperationDescIdDesc(Long projetId);
}
