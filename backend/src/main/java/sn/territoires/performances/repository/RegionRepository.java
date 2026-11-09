package sn.territoires.performances.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.territoires.performances.entity.Region;

public interface RegionRepository extends JpaRepository<Region, Integer> {
}
