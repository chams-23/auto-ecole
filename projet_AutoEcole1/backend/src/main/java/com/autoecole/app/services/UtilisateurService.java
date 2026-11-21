package com.autoecole.app.services;

import com.autoecole.app.dto.CreateUtilisateurRequest;
import com.autoecole.app.dto.UtilisateurDTO;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UtilisateurDTO> lister(Long tenantId) {
        return utilisateurRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<UtilisateurDTO> listerParRole(Long tenantId, Role role) {
        return utilisateurRepository.findAllByTenantIdAndRole(tenantId, role)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UtilisateurDTO getById(Long tenantId, Long id) {
        Utilisateur utilisateur = utilisateurRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return mapToDTO(utilisateur);
    }

    @Transactional
    public UtilisateurDTO creer(CreateUtilisateurRequest request, Long tenantId) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe deja");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .tenantId(tenantId)
                .role(request.role())
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .telephone(request.telephone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .statut(StatutUtilisateur.ACTIF)
                .build();

        Utilisateur sauvegarde = utilisateurRepository.save(utilisateur);
        return mapToDTO(sauvegarde);
    }

    @Transactional
    public UtilisateurDTO modifier(Long tenantId, Long id, UtilisateurDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        if (dto.role() != null) {
            utilisateur.setRole(dto.role());
        }
        if (dto.nom() != null) {
            utilisateur.setNom(dto.nom());
        }
        if (dto.prenom() != null) {
            utilisateur.setPrenom(dto.prenom());
        }
        if (dto.email() != null && !dto.email().equals(utilisateur.getEmail())) {
            if (utilisateurRepository.existsByEmail(dto.email())) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe deja");
            }
            utilisateur.setEmail(dto.email());
        }
        if (dto.telephone() != null) {
            utilisateur.setTelephone(dto.telephone());
        }
        if (dto.statut() != null) {
            utilisateur.setStatut(dto.statut());
        }

        Utilisateur misAJour = utilisateurRepository.save(utilisateur);
        return mapToDTO(misAJour);
    }

    @Transactional
    public void desactiver(Long tenantId, Long id) {
        Utilisateur utilisateur = utilisateurRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        utilisateur.setStatut(StatutUtilisateur.DESACTIVE);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void reactiver(Long tenantId, Long id) {
        Utilisateur utilisateur = utilisateurRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        utilisateur.setStatut(StatutUtilisateur.ACTIF);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void modifierMotDePasse(Long tenantId, Long id, String nouveauMdp) {
        Utilisateur utilisateur = utilisateurRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        utilisateur.setPasswordHash(passwordEncoder.encode(nouveauMdp));
        utilisateurRepository.save(utilisateur);
    }

    public UtilisateurDTO mapToDTO(Utilisateur u) {
        return new UtilisateurDTO(
                u.getId(),
                u.getTenantId(),
                u.getRole(),
                u.getNom(),
                u.getPrenom(),
                u.getEmail(),
                u.getTelephone(),
                u.getStatut()
        );
    }
}
