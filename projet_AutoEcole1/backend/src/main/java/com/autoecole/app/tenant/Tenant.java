package com.autoecole.app.tenant;

import com.autoecole.app.enums.PlanAbonnement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;

    @Column(name = "numero_agrement")
    private String numeroAgrement;

    @Column(name = "wilaya")
    private String wilaya;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_abonnement", nullable = false, length = 20)
    @Builder.Default
    private StatutAbonnement statutAbonnement = StatutAbonnement.ACTIF;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_abonnement", length = 30)
    @Builder.Default
    private PlanAbonnement planAbonnement = PlanAbonnement.BASIC;

    @Column(name = "date_debut_abonnement")
    private LocalDate dateDebutAbonnement;

    @Column(name = "date_fin_abonnement")
    private LocalDate dateFinAbonnement;

    @Column(name = "date_resiliation")
    private LocalDate dateResiliation;

    @Column(name = "date_suspension")
    private LocalDate dateSuspension;

    @Column(name = "montant_abonnement", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montantAbonnement = BigDecimal.ZERO;

    @Column(name = "admin_email", length = 150)
    private String adminEmail;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum StatutAbonnement {
        ACTIF,
        EXPIRE,
        SUSPENDU,
        RESILIE
    }
}
