package sn.territoires.performances.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.territoires.performances.entity.Realisation;

public interface RealisationRepository extends JpaRepository<Realisation, Long> {
    @Query("select r from Realisation r where (:collectiviteId is null or r.objectif.collectivite.id = :collectiviteId) "
            + "and (:annee is null or r.objectif.annee = :annee) "
            + "and (:statut is null or r.statut = :statut) order by r.saisiLe desc, r.id desc")
    List<Realisation> filtrer(@Param("collectiviteId") Long collectiviteId, @Param("annee") Integer annee,
                              @Param("statut") Realisation.Statut statut);

    @Query("select r from Realisation r where r.objectif.annee = :annee and r.statut = :statut")
    List<Realisation> parAnneeEtStatut(@Param("annee") Integer annee, @Param("statut") Realisation.Statut statut);

    List<Realisation> findByObjectifIdAndStatut(Long objectifId, Realisation.Statut statut);

    boolean existsByObjectifId(Long objectifId);
}
