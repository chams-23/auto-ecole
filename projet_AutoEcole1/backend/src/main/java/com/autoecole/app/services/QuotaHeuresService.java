package com.autoecole.app.services;

import com.autoecole.app.dto.QuotaHeuresDTO;
import com.autoecole.app.dto.StatutQuotaHeuresDTO;
import com.autoecole.app.dto.UpsertQuotaHeuresRequest;
import com.autoecole.app.entites.Candidat;
import com.autoecole.app.entites.QuotaHeures;
import com.autoecole.app.enums.CategoriePermis;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.CandidatRepository;
import com.autoecole.app.repositories.QuotaHeuresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotaHeuresService {

    private static final Map<CategoriePermis, int[]> DEFAULTS_CNEPS = new HashMap<>();
    static {
        DEFAULTS_CNEPS.put(CategoriePermis.B,  new int[]{60, 20});
        DEFAULTS_CNEPS.put(CategoriePermis.A,  new int[]{30, 15});
        DEFAULTS_CNEPS.put(CategoriePermis.A1, new int[]{20, 8});
        DEFAULTS_CNEPS.put(CategoriePermis.A2, new int[]{25, 12});
        DEFAULTS_CNEPS.put(CategoriePermis.C,  new int[]{80, 40});
        DEFAULTS_CNEPS.put(CategoriePermis.D,  new int[]{100, 50});
        DEFAULTS_CNEPS.put(CategoriePermis.EB, new int[]{60, 20});
        DEFAULTS_CNEPS.put(CategoriePermis.EC, new int[]{80, 40});
        DEFAULTS_CNEPS.put(CategoriePermis.ED, new int[]{100, 50});
    }

    private final QuotaHeuresRepository quotaHeuresRepository;
    private final CandidatRepository candidatRepository;

    public List<QuotaHeuresDTO> listerParTenant(Long tenantId) {
        return quotaHeuresRepository.findAllByTenantId(tenantId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public QuotaHeuresDTO getById(Long tenantId, Long id) {
        return quotaHeuresRepository.findByTenantIdAndId(tenantId, id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("QuotaHeures", "id", id));
    }

    @Transactional
    public QuotaHeuresDTO creer(Long tenantId, UpsertQuotaHeuresRequest req) {
        if (quotaHeuresRepository.existsByTenantIdAndCategoriePermis(tenantId, req.categoriePermis())) {
            throw new IllegalArgumentException("Un quota existe déjà pour la catégorie " + req.categoriePermis());
        }
        QuotaHeures entity = QuotaHeures.builder()
                .tenantId(tenantId)
                .categoriePermis(req.categoriePermis())
                .heuresCodeMin(req.heuresCodeMin() != null ? req.heuresCodeMin() : defautCode(req.categoriePermis()))
                .heuresConduiteMin(req.heuresConduiteMin() != null ? req.heuresConduiteMin() : defautConduite(req.categoriePermis()))
                .build();
        return mapToDTO(quotaHeuresRepository.save(entity));
    }

    @Transactional
    public QuotaHeuresDTO modifier(Long tenantId, Long id, UpsertQuotaHeuresRequest req) {
        QuotaHeures entity = quotaHeuresRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("QuotaHeures", "id", id));
        entity.setHeuresCodeMin(req.heuresCodeMin());
        entity.setHeuresConduiteMin(req.heuresConduiteMin());
        return mapToDTO(quotaHeuresRepository.save(entity));
    }

    @Transactional
    public void supprimer(Long tenantId, Long id) {
        QuotaHeures entity = quotaHeuresRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("QuotaHeures", "id", id));
        quotaHeuresRepository.delete(entity);
    }

    public StatutQuotaHeuresDTO calculerStatutPourCandidat(Long tenantId, Long candidatId) {
        Candidat candidat = candidatRepository.findByTenantIdAndId(tenantId, candidatId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat", "id", candidatId));
        return calculerStatut(
                tenantId,
                candidat.getCategoriePermis(),
                candidat.getHeuresTheorieValidees() != null ? candidat.getHeuresTheorieValidees() : 0,
                candidat.getHeuresConduiteValidees() != null ? candidat.getHeuresConduiteValidees() : 0
        );
    }

    public StatutQuotaHeuresDTO calculerStatut(Long tenantId, CategoriePermis categorie, int heuresCodeValidees, int heuresConduiteValidees) {
        int quotaCode = defautCode(categorie);
        int quotaConduite = defautConduite(categorie);

        QuotaHeures quota = quotaHeuresRepository.findByTenantIdAndCategoriePermis(tenantId, categorie).orElse(null);
        if (quota != null) {
            quotaCode = quota.getHeuresCodeMin() != null ? quota.getHeuresCodeMin() : quotaCode;
            quotaConduite = quota.getHeuresConduiteMin() != null ? quota.getHeuresConduiteMin() : quotaConduite;
        }

        boolean codeAtteint = heuresCodeValidees >= quotaCode;
        boolean conduiteAtteint = heuresConduiteValidees >= quotaConduite;
        return new StatutQuotaHeuresDTO(
                categorie,
                quotaCode,
                quotaConduite,
                heuresCodeValidees,
                heuresConduiteValidees,
                codeAtteint,
                conduiteAtteint,
                codeAtteint && conduiteAtteint
        );
    }

    private int defautCode(CategoriePermis c) {
        int[] d = DEFAULTS_CNEPS.get(c);
        return d != null ? d[0] : 0;
    }

    private int defautConduite(CategoriePermis c) {
        int[] d = DEFAULTS_CNEPS.get(c);
        return d != null ? d[1] : 0;
    }

    private QuotaHeuresDTO mapToDTO(QuotaHeures e) {
        return new QuotaHeuresDTO(
                e.getId(),
                e.getTenantId(),
                e.getCategoriePermis(),
                e.getHeuresCodeMin(),
                e.getHeuresConduiteMin(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
