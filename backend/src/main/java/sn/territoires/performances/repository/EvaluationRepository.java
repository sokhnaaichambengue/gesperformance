package sn.territoires.performances.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.territoires.performances.entity.Evaluation;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByRealisationIdOrderByCalculeLeDescIdDesc(Long realisationId);
}
