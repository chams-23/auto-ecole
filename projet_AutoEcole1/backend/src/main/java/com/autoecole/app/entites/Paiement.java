package com.autoecole.app.entites;

import com.autoecole.app.enums.ModePaiement;
import com.autoecole.app.enums.TypePaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "paiements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "candidat_id", nullable = false)
    private Long candidatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", insertable = false, updatable = false)
    private Candidat candidat;

    @Column(name = "montant", nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 15)
    private ModePaiement modePaiement;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TypePaiement type;

    @Column(name = "recu_url", length = 255)
    private String recuUrl;

    @Column(name = "numero_recu", length = 50)
    private String numeroRecu;

    @Column(name = "est_annule")
    @Builder.Default
    private Boolean estAnnule = false;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "nombre_relances")
    @Builder.Default
    private Integer nombreRelances = 0;

    @Column(name = "date_derniere_relance")
    private LocalDateTime dateDerniereRelance;

    @PrePersist
    public void genererNumeroRecu() {
        if (this.numeroRecu == null || this.numeroRecu.isBlank()) {
            String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
            this.numeroRecu = "REC-" + datePart + "-" + randomPart;
        }
    }
}
