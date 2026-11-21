package com.autoecole.app.services;

import com.autoecole.app.entites.Paiement;
import com.autoecole.app.entites.RelanceImpaye;
import com.autoecole.app.tenant.Tenant;
import com.autoecole.app.enums.TypeEvenementNotification;
import com.autoecole.app.repositories.PaiementRepository;
import com.autoecole.app.repositories.RelanceImpayeRepository;
import com.autoecole.app.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelanceService {

    private final TenantRepository tenantRepository;
    private final PaiementRepository paiementRepository;
    private final RelanceImpayeRepository relanceImpayeRepository;
    private final NotificationService notificationService;

    private final int MAX_RELANCES = 3;
    private final int DELAI_JOURS = 7; // interval between relances

    @Scheduled(cron = "0 0 7 * * *") // every day at 07:00
    @Transactional
    public void runDailyRelances() {
        List<Tenant> tenants = tenantRepository.findAll();
        for (Tenant t : tenants) {
            processTenant(t.getId());
        }
    }

    private void processTenant(Long tenantId) {
        List<Paiement> paiements = paiementRepository.findAllByTenantId(tenantId);
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (Paiement p : paiements) {
            if (p.getEstAnnule() != null && p.getEstAnnule()) continue;
            if (p.getDateEcheance() == null) continue;
            if (!p.getDateEcheance().isBefore(today)) continue; // not yet due

            Integer nb = p.getNombreRelances() == null ? 0 : p.getNombreRelances();
            LocalDateTime last = p.getDateDerniereRelance();
            if (nb >= MAX_RELANCES) continue;
            if (last != null && last.isAfter(now.minusDays(DELAI_JOURS))) continue;

            int numero = nb + 1;
            RelanceImpaye rel = RelanceImpaye.builder()
                    .tenantId(tenantId)
                    .paiementId(p.getId())
                    .candidatId(p.getCandidatId())
                    .numeroRelance(numero)
                    .message("Relance automatique: montant " + p.getMontant() + " DH, echeance '" + p.getDateEcheance() + "'")
                    .build();
            relanceImpayeRepository.save(rel);

            p.setNombreRelances(numero);
            p.setDateDerniereRelance(now);
            paiementRepository.save(p);

            // create a notification to candidate
            if (p.getCandidat() != null && p.getCandidat().getUtilisateurId() != null) {
                notificationService.creerNotification(tenantId, p.getCandidat().getUtilisateurId(), TypeEvenementNotification.PAIEMENT_EN_RETARD,
                        "Votre paiement de " + p.getMontant() + " DH est en retard. Relance #" + numero);
            }
        }
    }
}
