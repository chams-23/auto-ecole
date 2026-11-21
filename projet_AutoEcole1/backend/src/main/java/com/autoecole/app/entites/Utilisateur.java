package com.autoecole.app.entites;

import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.tenant.Tenant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "utilisateurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    @Builder.Default
    private StatutUtilisateur statut = StatutUtilisateur.ACTIF;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
