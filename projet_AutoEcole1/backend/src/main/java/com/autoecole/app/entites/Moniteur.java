package com.autoecole.app.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "moniteurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Moniteur {

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

    @Column(name = "numero_carte_pro", nullable = false, length = 50, unique = true)
    private String numeroCartePro;

    @Column(name = "categories_autorisees", length = 255)
    private String categoriesAutorisees;

    @Column(name = "vehicule_id")
    private Long vehiculeId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", insertable = false, updatable = false)
    private Vehicule vehicule;
}
