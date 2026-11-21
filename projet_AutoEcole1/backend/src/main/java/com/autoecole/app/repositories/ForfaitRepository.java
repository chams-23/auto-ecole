package com.autoecole.app.repositories;

import com.autoecole.app.entites.Forfait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForfaitRepository extends JpaRepository<Forfait, Long> {

    List<Forfait> findAllByTenantId(Long tenantId);

    Optional<Forfait> findByTenantIdAndId(Long tenantId, Long id);

    List<Forfait> findAllByTenantIdAndCategoriePermis(Long tenantId, com.autoecole.app.enums.CategoriePermis categoriePermis);
}
