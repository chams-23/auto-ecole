package com.autoecole.app.entites;

import com.autoecole.app.enums.CategoriePermis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "forfaits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Forfait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_permis", nullable = false, length = 5)
    private CategoriePermis categoriePermis;

    @Column(name = "heures_code_min", nullable = false)
    @Builder.Default
    private Integer heuresCodeMin = 0;

    @Column(name = "heures_conduite_min", nullable = false)
    @Builder.Default
    private Integer heuresConduiteMin = 20;

    @Column(name = "prix", nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;
}
