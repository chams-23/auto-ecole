package com.autoecole.app.services;

import com.autoecole.app.dto.BalanceTresorerieDTO;
import com.autoecole.app.entites.Charge;
import com.autoecole.app.entites.Paiement;
import com.autoecole.app.enums.TypePaiement;
import com.autoecole.app.repositories.ChargeRepository;
import com.autoecole.app.repositories.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TresorerieService {

    private final PaiementRepository paiementRepository;
    private final ChargeRepository chargeRepository;

    public BalanceTresorerieDTO calculerBalance(Long tenantId, String periode) {
        LocalDate dateDebut;
        LocalDate dateFin;
        YearMonth maintenant = YearMonth.now();

        switch (periode != null ? periode.toUpperCase() : "MOIS") {
            case "SEMAINE" -> {
                dateDebut = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
                dateFin = dateDebut.plusDays(6);
            }
            case "TRIMESTRE" -> {
                int trimestre = (maintenant.getMonthValue() - 1) / 3;
                dateDebut = maintenant.atDay(1).withMonth(trimestre * 3 + 1);
                dateFin = dateDebut.plusMonths(3).minusDays(1);
            }
            case "ANNEE" -> {
                dateDebut = maintenant.atDay(1).withMonth(1);
                dateFin = dateDebut.plusYears(1).minusDays(1);
            }
            default -> {
                dateDebut = maintenant.atDay(1);
                dateFin = maintenant.atEndOfMonth();
            }
        }

        return calculerBalancePeriode(tenantId, dateDebut, dateFin);
    }

    public BalanceTresorerieDTO calculerBalancePeriode(Long tenantId, LocalDate dateDebut, LocalDate dateFin) {
        List<Paiement> paiements = paiementRepository.findAllByTenantIdAndDateBetween(tenantId, dateDebut, dateFin);
        List<Charge> charges = chargeRepository.findAllByTenantIdAndDateBetween(tenantId, dateDebut, dateFin);

        BigDecimal totalPaiements = BigDecimal.ZERO;
        BigDecimal totalCharges = BigDecimal.ZERO;
        BigDecimal totalAvance = BigDecimal.ZERO;
        BigDecimal totalVersement = BigDecimal.ZERO;

        for (Paiement p : paiements) {
            if (p.getMontant() != null) {
                totalPaiements = totalPaiements.add(p.getMontant());
                if (p.getType() == TypePaiement.AVANCE) {
                    totalAvance = totalAvance.add(p.getMontant());
                } else if (p.getType() == TypePaiement.VERSEMENT) {
                    totalVersement = totalVersement.add(p.getMontant());
                }
            }
        }

        for (Charge c : charges) {
            if (c.getMontant() != null) {
                totalCharges = totalCharges.add(c.getMontant());
            }
        }

        BigDecimal soldeNet = totalPaiements.subtract(totalCharges);

        return new BalanceTresorerieDTO(
                dateDebut, dateFin,
                totalPaiements, totalCharges, soldeNet,
                (long) paiements.size(), (long) charges.size(),
                totalAvance, totalVersement
        );
    }
}
