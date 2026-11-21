package com.autoecole.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModifierMotDePasseRequest(
        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caracteres")
        String nouveauMdp
) {}
