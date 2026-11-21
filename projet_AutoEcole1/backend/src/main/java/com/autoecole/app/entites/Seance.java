package com.autoecole.app.entites;

import com.autoecole.app.enums.StatutValidation;
import com.autoecole.app.enums.TypeSeance;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "seances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TypeSeance type;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(name = "candidat_id", nullable = false)
    private Long candidatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", insertable = false, updatable = false)
    private Candidat candidat;

    @Column(name = "intervenant_id", nullable = false)
    private Long intervenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervenant_id", insertable = false, updatable = false)
    private Utilisateur intervenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", nullable = false, length = 25)
    @Builder.Default
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    @Column(name = "kilometrage")
    private Double kilometrage;

    @Column(name = "observations", length = 500, columnDefinition = "TEXT")
    private String observations;
}
