package com.autoecole.app.repositories;

import com.autoecole.app.entites.QuotaHeures;
import com.autoecole.app.enums.CategoriePermis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaHeuresRepository extends JpaRepository<QuotaHeures, Long> {

    List<QuotaHeures> findAllByTenantId(Long tenantId);

    Optional<QuotaHeures> findByTenantIdAndId(Long tenantId, Long id);

    Optional<QuotaHeures> findByTenantIdAndCategoriePermis(Long tenantId, CategoriePermis categoriePermis);

    boolean existsByTenantIdAndCategoriePermis(Long tenantId, CategoriePermis categoriePermis);
}
