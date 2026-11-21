package com.autoecole.app.repositories;

import com.autoecole.app.entites.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {

    List<Candidat> findAllByTenantId(Long tenantId);

    Optional<Candidat> findByTenantIdAndId(Long tenantId, Long id);

    Optional<Candidat> findByUtilisateurId(Long utilisateurId);

    Optional<Candidat> findByCin(String cin);

    List<Candidat> findAllByTenantIdAndStatutParcours(Long tenantId, com.autoecole.app.enums.StatutParcours statutParcours);

    Optional<Candidat> findByTenantIdAndCin(Long tenantId, String cin);
}
