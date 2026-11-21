package com.autoecole.app.dto;

import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutUtilisateur;

public record UtilisateurDTO(
        Long id,
        Long tenantId,
        Role role,
        String nom,
        String prenom,
        String email,
        String telephone,
        StatutUtilisateur statut
) {}
