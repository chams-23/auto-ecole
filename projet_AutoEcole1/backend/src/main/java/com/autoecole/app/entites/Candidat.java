package com.autoecole.app.entites;

import com.autoecole.app.enums.CategoriePermis;
import com.autoecole.app.enums.ResultatExamen;
import com.autoecole.app.enums.StatutParcours;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "candidats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false, unique = true)
    private Long utilisateurId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "cin", nullable = false, length = 15, unique = true)
    private String cin;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_permis", nullable = false, length = 5)
    private CategoriePermis categoriePermis;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_parcours", nullable = false, length = 25)
    @Builder.Default
    private StatutParcours statutParcours = StatutParcours.INSCRIT;

    @Column(name = "forfait_id")
    private Long forfaitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forfait_id", insertable = false, updatable = false)
    private Forfait forfait;

    @Column(name = "heures_conduite_validees", nullable = false)
    @Builder.Default
    private Integer heuresConduiteValidees = 0;

    @Column(name = "heures_theorie_validees", nullable = false)
    @Builder.Default
    private Integer heuresTheorieValidees = 0;

    @Column(name = "date_inscription")
    @Builder.Default
    private LocalDate dateInscription = LocalDate.now();

    @Column(name = "date_examen_theorie")
    private LocalDate dateExamenTheorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat_examen_theorie", length = 15)
    @Builder.Default
    private ResultatExamen resultatExamenTheorie = ResultatExamen.EN_ATTENTE;

    @Column(name = "nombre_tentatives_theorie")
    @Builder.Default
    private Integer nombreTentativesTheorie = 0;

    @Column(name = "date_examen_pratique")
    private LocalDate dateExamenPratique;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat_examen_pratique", length = 15)
    @Builder.Default
    private ResultatExamen resultatExamenPratique = ResultatExamen.EN_ATTENTE;

    @Column(name = "nombre_tentatives_pratique")
    @Builder.Default
    private Integer nombreTentativesPratique = 0;
}
