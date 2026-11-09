package sn.territoires.performances.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.territoires.performances.entity.Collectivite;

public interface CollectiviteRepository extends JpaRepository<Collectivite, Long> {
    @Query("select c from Collectivite c where (:type is null or c.type = :type) "
            + "and (:regionId is null or c.region.id = :regionId) order by c.nom")
    List<Collectivite> filtrer(@Param("type") Collectivite.Type type, @Param("regionId") Integer regionId);
}
