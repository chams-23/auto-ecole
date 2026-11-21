package com.autoecole.app.entites;

import com.autoecole.app.enums.StatutValidation;
import com.autoecole.app.enums.TypeDocumentCandidat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "documents_candidat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCandidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidat_id", nullable = false)
    private Long candidatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", insertable = false, updatable = false)
    private Candidat candidat;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false, length = 50)
    private TypeDocumentCandidat typeDocument;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "chemin_fichier", nullable = false, length = 500)
    private String cheminFichier;

    @Column(name = "taille_octets")
    @Builder.Default
    private Long tailleOctets = 0L;

    @Column(name = "type_mime", length = 100)
    private String typeMime;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", length = 20)
    @Builder.Default
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    @Column(name = "valide_par")
    private Long validePar;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
