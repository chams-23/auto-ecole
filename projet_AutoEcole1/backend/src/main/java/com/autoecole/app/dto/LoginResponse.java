package com.autoecole.app.dto;

public record LoginResponse(
        String token,
        UtilisateurDTO user
) {}
