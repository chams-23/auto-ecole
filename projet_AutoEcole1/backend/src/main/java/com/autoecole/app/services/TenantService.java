package com.autoecole.app.services;

import com.autoecole.app.enums.PlanAbonnement;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.tenant.Tenant;
import com.autoecole.app.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public Tenant creer(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public Tenant getById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", id));
    }

    public List<Tenant> lister() {
        return tenantRepository.findAll();
    }

    public List<Tenant> listerTous() {
        return tenantRepository.findAll();
    }

    @Transactional
    public Tenant modifierStatutAbonnement(Long id, Tenant.StatutAbonnement nouveauStatut) {
        Tenant tenant = getById(id);
        tenant.setStatutAbonnement(nouveauStatut);
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant activerAbonnement(Long id) {
        Tenant tenant = getById(id);
        tenant.setStatutAbonnement(Tenant.StatutAbonnement.ACTIF);
        tenant.setDateSuspension(null);
        tenant.setDateResiliation(null);
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant suspendreAbonnement(Long id, LocalDate dateSuspension) {
        Tenant tenant = getById(id);
        tenant.setStatutAbonnement(Tenant.StatutAbonnement.SUSPENDU);
        tenant.setDateSuspension(dateSuspension);
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant resilierAbonnement(Long id, LocalDate dateResiliation) {
        Tenant tenant = getById(id);
        tenant.setStatutAbonnement(Tenant.StatutAbonnement.RESILIE);
        tenant.setDateResiliation(dateResiliation);
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant modifierPlanAbonnement(Long id, PlanAbonnement plan, LocalDate debut, LocalDate fin, BigDecimal montant) {
        Tenant tenant = getById(id);
        tenant.setPlanAbonnement(plan);
        tenant.setDateDebutAbonnement(debut);
        tenant.setDateFinAbonnement(fin);
        tenant.setMontantAbonnement(montant);
        return tenantRepository.save(tenant);
    }
}
