package com.autoecole.app.entites;

import com.autoecole.app.enums.CategoriePermis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quota_heures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaHeures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_permis", nullable = false, length = 10)
    private CategoriePermis categoriePermis;

    @Column(name = "heures_code_min", nullable = false)
    @Builder.Default
    private Integer heuresCodeMin = 0;

    @Column(name = "heures_conduite_min", nullable = false)
    @Builder.Default
    private Integer heuresConduiteMin = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
