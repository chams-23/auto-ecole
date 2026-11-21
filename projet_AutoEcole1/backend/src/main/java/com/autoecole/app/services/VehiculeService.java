package com.autoecole.app.services;

import com.autoecole.app.dto.CreateVehiculeRequest;
import com.autoecole.app.dto.VehiculeAlerteDTO;
import com.autoecole.app.dto.VehiculeDTO;
import com.autoecole.app.entites.Moniteur;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.entites.Vehicule;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.MoniteurRepository;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.repositories.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final MoniteurRepository moniteurRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<VehiculeDTO> lister(Long tenantId) {
        return vehiculeRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public VehiculeDTO getById(Long tenantId, Long id) {
        Vehicule vehicule = vehiculeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", "id", id));
        return mapToDTO(vehicule);
    }

    @Transactional
    public VehiculeDTO creer(CreateVehiculeRequest request, Long tenantId) {
        if (vehiculeRepository.findByImmatriculation(request.immatriculation()).isPresent()) {
            throw new IllegalArgumentException("Un vehicule avec cette immatriculation existe deja");
        }

        Vehicule vehicule = Vehicule.builder()
                .tenantId(tenantId)
                .immatriculation(request.immatriculation())
                .categoriePermis(request.categoriePermis())
                .dateMiseService(request.dateMiseService())
                .dateControleTechnique(request.dateControleTechnique())
                .dateExpirationAssurance(request.dateExpirationAssurance())
                .build();

        Vehicule sauvegarde = vehiculeRepository.save(vehicule);

        if (request.moniteurId() != null) {
            attribuerMoniteurInterne(tenantId, sauvegarde.getId(), request.moniteurId());
            sauvegarde = vehiculeRepository.findByTenantIdAndId(tenantId, sauvegarde.getId()).orElse(sauvegarde);
        }

        return mapToDTO(sauvegarde);
    }

    @Transactional
    public VehiculeDTO modifier(Long tenantId, Long id, VehiculeDTO dto) {
        Vehicule vehicule = vehiculeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", "id", id));

        if (dto.immatriculation() != null && !dto.immatriculation().equals(vehicule.getImmatriculation())) {
            if (vehiculeRepository.findByImmatriculation(dto.immatriculation()).isPresent()) {
                throw new IllegalArgumentException("Un vehicule avec cette immatriculation existe deja");
            }
            vehicule.setImmatriculation(dto.immatriculation());
        }
        if (dto.categoriePermis() != null) {
            vehicule.setCategoriePermis(dto.categoriePermis());
        }
        if (dto.dateMiseService() != null) {
            vehicule.setDateMiseService(dto.dateMiseService());
        }
        if (dto.dateControleTechnique() != null) {
            vehicule.setDateControleTechnique(dto.dateControleTechnique());
        }
        if (dto.dateExpirationAssurance() != null) {
            vehicule.setDateExpirationAssurance(dto.dateExpirationAssurance());
        }

        return mapToDTO(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public VehiculeDTO attribuerMoniteur(Long tenantId, Long vehiculeId, Long moniteurId) {
        attribuerMoniteurInterne(tenantId, vehiculeId, moniteurId);
        Vehicule vehicule = vehiculeRepository.findByTenantIdAndId(tenantId, vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", "id", vehiculeId));
        return mapToDTO(vehicule);
    }

    private void attribuerMoniteurInterne(Long tenantId, Long vehiculeId, Long moniteurId) {
        Vehicule vehicule = vehiculeRepository.findByTenantIdAndId(tenantId, vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", "id", vehiculeId));

        if (moniteurId != null) {
            Moniteur nouveauMoniteur = moniteurRepository.findByTenantIdAndId(tenantId, moniteurId)
                    .orElseThrow(() -> new ResourceNotFoundException("Moniteur", "id", moniteurId));

            Moniteur ancienMoniteur = moniteurRepository.findByVehiculeId(vehiculeId).orElse(null);
            if (ancienMoniteur != null && !ancienMoniteur.getId().equals(moniteurId)) {
                ancienMoniteur.setVehiculeId(null);
                moniteurRepository.save(ancienMoniteur);
            }

            nouveauMoniteur.setVehiculeId(vehiculeId);
            moniteurRepository.save(nouveauMoniteur);
        } else {
            Moniteur ancienMoniteur = moniteurRepository.findByVehiculeId(vehiculeId).orElse(null);
            if (ancienMoniteur != null) {
                ancienMoniteur.setVehiculeId(null);
                moniteurRepository.save(ancienMoniteur);
            }
        }
    }

    public List<VehiculeAlerteDTO> getAlertes(Long tenantId) {
        List<Vehicule> vehicules = vehiculeRepository.findAllByTenantId(tenantId);
        List<VehiculeAlerteDTO> alertes = new ArrayList<>();
        LocalDate aujourdHui = LocalDate.now();
        LocalDate dans30Jours = aujourdHui.plusDays(30);

        for (Vehicule v : vehicules) {
            if (v.getDateControleTechnique() != null) {
                long jours = ChronoUnit.DAYS.between(aujourdHui, v.getDateControleTechnique());
                if (jours <= 30) {
                    String niveau = jours < 0 ? "CRITIQUE" : (jours <= 7 ? "URGENT" : "AVERTISSEMENT");
                    alertes.add(new VehiculeAlerteDTO(
                            v.getId(), v.getImmatriculation(),
                            "CONTROLE_TECHNIQUE",
                            "Controle technique",
                            v.getDateControleTechnique(),
                            jours,
                            niveau
                    ));
                }
            }

            if (v.getDateExpirationAssurance() != null) {
                long jours = ChronoUnit.DAYS.between(aujourdHui, v.getDateExpirationAssurance());
                if (jours <= 30) {
                    String niveau = jours < 0 ? "CRITIQUE" : (jours <= 7 ? "URGENT" : "AVERTISSEMENT");
                    alertes.add(new VehiculeAlerteDTO(
                            v.getId(), v.getImmatriculation(),
                            "ASSURANCE",
                            "Expiration assurance",
                            v.getDateExpirationAssurance(),
                            jours,
                            niveau
                    ));
                }
            }
        }

        alertes.sort((a, b) -> Long.compare(a.joursRestants() == null ? Long.MIN_VALUE : a.joursRestants(),
                b.joursRestants() == null ? Long.MIN_VALUE : b.joursRestants()));
        return alertes;
    }

    private VehiculeDTO mapToDTO(Vehicule v) {
        Moniteur moniteur = v.getMoniteur();
        Long moniteurId = null;
        String monNom = null;
        String monPrenom = null;

        if (moniteur != null) {
            moniteurId = moniteur.getId();
            Utilisateur u = utilisateurRepository.findById(moniteur.getUtilisateurId()).orElse(null);
            if (u != null) {
                monNom = u.getNom();
                monPrenom = u.getPrenom();
            }
        } else {
            Moniteur mByVeh = moniteurRepository.findByVehiculeId(v.getId()).orElse(null);
            if (mByVeh != null) {
                moniteurId = mByVeh.getId();
                Utilisateur u = utilisateurRepository.findById(mByVeh.getUtilisateurId()).orElse(null);
                if (u != null) {
                    monNom = u.getNom();
                    monPrenom = u.getPrenom();
                }
            }
        }

        return new VehiculeDTO(
                v.getId(),
                v.getTenantId(),
                v.getImmatriculation(),
                v.getCategoriePermis(),
                v.getDateMiseService(),
                v.getDateControleTechnique(),
                v.getDateExpirationAssurance(),
                moniteurId,
                monNom,
                monPrenom
        );
    }
}
