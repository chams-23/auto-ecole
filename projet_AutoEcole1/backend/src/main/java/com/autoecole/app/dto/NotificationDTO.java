package com.autoecole.app.dto;

import com.autoecole.app.enums.TypeEvenementNotification;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        Long destinataireId,
        TypeEvenementNotification typeEvenement,
        String contenu,
        Boolean statutLecture,
        LocalDateTime date
) {}
