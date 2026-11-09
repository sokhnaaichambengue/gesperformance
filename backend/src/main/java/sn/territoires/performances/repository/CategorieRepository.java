package sn.territoires.performances.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.territoires.performances.entity.CategorieIndicateur;

public interface CategorieRepository extends JpaRepository<CategorieIndicateur, Integer> {
}
