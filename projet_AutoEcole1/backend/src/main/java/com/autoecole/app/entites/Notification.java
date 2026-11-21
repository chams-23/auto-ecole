package com.autoecole.app.entites;

import com.autoecole.app.enums.TypeEvenementNotification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "destinataire_id", nullable = false)
    private Long destinataireId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", insertable = false, updatable = false)
    private Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", nullable = false, length = 25)
    private TypeEvenementNotification typeEvenement;

    @Column(name = "contenu", nullable = false, length = 500)
    private String contenu;

    @Column(name = "statut_lecture", nullable = false)
    @Builder.Default
    private Boolean statutLecture = false;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;
}
