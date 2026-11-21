package com.autoecole.app.entites;

import com.autoecole.app.enums.ActionAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(name = "email_utilisateur", length = 150)
    private String emailUtilisateur;

    @Column(name = "role_utilisateur", length = 30)
    private String roleUtilisateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 100)
    private ActionAudit action;

    @Column(name = "entite_type", length = 50)
    private String entiteType;

    @Column(name = "entite_id")
    private Long entiteId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "avant", columnDefinition = "TEXT")
    @Lob
    private String avant;

    @Column(name = "apres", columnDefinition = "TEXT")
    @Lob
    private String apres;

    @Column(name = "adresse_ip", length = 45)
    private String adresseIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "date_action", nullable = false)
    @Builder.Default
    private LocalDateTime dateAction = LocalDateTime.now();
}
