package sn.territoires.performances.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.territoires.performances.entity.Objectif;

public interface ObjectifRepository extends JpaRepository<Objectif, Long> {
    @Query("select o from Objectif o where (:collectiviteId is null or o.collectivite.id = :collectiviteId) "
            + "and (:indicateurId is null or o.indicateur.id = :indicateurId) "
            + "and (:annee is null or o.annee = :annee) order by o.annee desc, o.collectivite.nom, o.indicateur.nom")
    List<Objectif> filtrer(@Param("collectiviteId") Long collectiviteId,
                           @Param("indicateurId") Long indicateurId, @Param("annee") Integer annee);

    @Query("select o from Objectif o join fetch o.indicateur join fetch o.collectivite c join fetch c.region "
            + "where o.annee = :annee")
    List<Objectif> parAnnee(@Param("annee") Integer annee);

    @Query("select distinct o.annee from Objectif o order by o.annee")
    List<Integer> annees();

    boolean existsByCollectiviteId(Long collectiviteId);

    List<Objectif> findByIndicateurId(Long indicateurId);
}
