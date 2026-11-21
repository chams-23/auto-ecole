package com.autoecole.app.dto;

import com.autoecole.app.enums.CategoriePermis;
import com.autoecole.app.enums.ResultatExamen;
import com.autoecole.app.enums.StatutParcours;

import java.time.LocalDate;

public record CandidatDTO(
        Long id,
        Long utilisateurId,
        Long tenantId,
        String cin,
        CategoriePermis categoriePermis,
        StatutParcours statutParcours,
        Long forfaitId,
        Integer heuresConduiteValidees,
        Integer heuresTheorieValidees,
        String nom,
        String prenom,
        String email,
        String telephone,
        LocalDate dateExamenTheorie,
        ResultatExamen resultatExamenTheorie,
        Integer nombreTentativesTheorie,
        LocalDate dateExamenPratique,
        ResultatExamen resultatExamenPratique,
        Integer nombreTentativesPratique
) {}
