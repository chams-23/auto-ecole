package com.autoecole.app.dto;

import com.autoecole.app.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUtilisateurRequest(
        @NotNull(message = "Le role est obligatoire")
        Role role,
        @NotBlank(message = "Le nom est obligatoire")
        String nom,
        @NotBlank(message = "Le prenom est obligatoire")
        String prenom,
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,
        String telephone,
        @NotBlank(message = "Le mot de passe est obligatoire")
        String password
) {}
