package com.autoecole.app.repositories;

import com.autoecole.app.entites.DocumentCandidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentCandidatRepository extends JpaRepository<DocumentCandidat, Long> {

    List<DocumentCandidat> findAllByTenantId(Long tenantId);

    Optional<DocumentCandidat> findByTenantIdAndId(Long tenantId, Long id);

    List<DocumentCandidat> findAllByTenantIdAndCandidatId(Long tenantId, Long candidatId);

    List<DocumentCandidat> findAllByTenantIdAndTypeDocument(Long tenantId, com.autoecole.app.enums.TypeDocumentCandidat typeDocument);

    List<DocumentCandidat> findAllByTenantIdAndStatutValidation(Long tenantId, com.autoecole.app.enums.StatutValidation statutValidation);

    List<DocumentCandidat> findAllByTenantIdAndCandidatIdAndTypeDocument(Long tenantId, Long candidatId, com.autoecole.app.enums.TypeDocumentCandidat typeDocument);
}
