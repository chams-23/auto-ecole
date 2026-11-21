package com.autoecole.app.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "relances_impayes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelanceImpaye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paiement_id", nullable = false)
    private Long paiementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_id", insertable = false, updatable = false)
    private Paiement paiement;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "candidat_id", nullable = false)
    private Long candidatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", insertable = false, updatable = false)
    private Candidat candidat;

    @Column(name = "numero_relance", nullable = false)
    private Integer numeroRelance;

    @Column(name = "date_envoi", nullable = false)
    @Builder.Default
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @Column(name = "canal_envoi", length = 20)
    @Builder.Default
    private String canalEnvoi = "EMAIL";

    @Column(name = "statut_envoi", length = 20)
    @Builder.Default
    private String statutEnvoi = "ENVOYE";

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
}
