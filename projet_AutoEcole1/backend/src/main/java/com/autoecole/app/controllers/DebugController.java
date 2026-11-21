package com.autoecole.app.controllers;

import com.autoecole.app.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Profile("dev")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class DebugController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> debug() {
        Map<String, Object> res = new HashMap<>();
        res.put("tenantId_from_context", TenantContext.getTenantId());
        res.put("count_tenants", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tenants", Long.class));
        res.put("count_utilisateurs", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM utilisateurs", Long.class));
        res.put("count_candidats_all", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM candidats", Long.class));
        Long tid = TenantContext.getTenantId();
        if (tid != null) {
            res.put("count_candidats_by_tenant", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM candidats WHERE tenant_id = ?", Long.class, tid));
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, utilisateur_id, tenant_id, cin FROM candidats WHERE tenant_id = ? LIMIT 5", tid);
                res.put("candidats_sample", rows);
            } catch (Exception e) {
                res.put("candidats_sample_error", e.toString());
            }
        } else {
            res.put("count_candidats_by_tenant", null);
        }
        return ResponseEntity.ok(res);
    }
}
