package com.autoecole.app.entites;

import com.autoecole.app.enums.CategorieCharge;
import com.autoecole.app.enums.PeriodiciteCharge;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "charges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 15)
    private CategorieCharge categorie;

    @Column(name = "montant", nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicite", nullable = false, length = 15)
    private PeriodiciteCharge periodicite;

    @Column(name = "justificatif_url", length = 255)
    private String justificatifUrl;
}
