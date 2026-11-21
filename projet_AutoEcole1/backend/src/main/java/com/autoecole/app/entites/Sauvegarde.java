package com.autoecole.app.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sauvegardes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sauvegarde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "type_sauvegarde", nullable = false, length = 20)
    @Builder.Default
    private String typeSauvegarde = "COMPLETE";

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "chemin_fichier", length = 500)
    private String cheminFichier;

    @Column(name = "taille_octets")
    @Builder.Default
    private Long tailleOctets = 0L;

    @Column(name = "statut", length = 20)
    @Builder.Default
    private String statut = "EN_COURS";

    @Column(name = "declencheur", length = 20)
    @Builder.Default
    private String declencheur = "AUTO";

    @Column(name = "erreur_message", columnDefinition = "TEXT")
    private String erreurMessage;

    @Column(name = "date_debut")
    @Builder.Default
    private LocalDateTime dateDebut = LocalDateTime.now();

    @Column(name = "date_fin")
    private LocalDateTime dateFin;
}
