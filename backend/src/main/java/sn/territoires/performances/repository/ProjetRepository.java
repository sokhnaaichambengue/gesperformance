package sn.territoires.performances.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.territoires.performances.entity.Collectivite;
import sn.territoires.performances.entity.Projet;

public interface ProjetRepository extends JpaRepository<Projet, Long> {
    @Query("select p from Projet p where (:collectiviteId is null or p.collectivite.id = :collectiviteId) "
            + "and (:domaineId is null or p.domaine.id = :domaineId) "
            + "and (:statut is null or p.statut = :statut) order by p.nom")
    List<Projet> filtrer(@Param("collectiviteId") Long collectiviteId, @Param("domaineId") Integer domaineId,
                         @Param("statut") Projet.Statut statut);

    @Query("select p from Projet p where (:regionId is null or p.collectivite.region.id = :regionId) "
            + "and (:type is null or p.collectivite.type = :type) "
            + "and (:domaineId is null or p.domaine.id = :domaineId)")
    List<Projet> pourTableauDeBord(@Param("regionId") Integer regionId, @Param("type") Collectivite.Type type,
                                   @Param("domaineId") Integer domaineId);

    boolean existsByCollectiviteId(Long collectiviteId);
}
