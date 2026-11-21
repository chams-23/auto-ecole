package com.autoecole.app.entites;

import com.autoecole.app.enums.CategoriePermis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "vehicules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "immatriculation", nullable = false, length = 20, unique = true)
    private String immatriculation;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_permis", nullable = false, length = 5)
    private CategoriePermis categoriePermis;

    @Column(name = "date_mise_service")
    private LocalDate dateMiseService;

    @Column(name = "date_controle_technique")
    private LocalDate dateControleTechnique;

    @Column(name = "date_expiration_assurance")
    private LocalDate dateExpirationAssurance;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "vehicule")
    private Moniteur moniteur;
}
