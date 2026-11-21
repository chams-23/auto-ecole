package com.autoecole.app.controllers;

import com.autoecole.app.services.ExportService;
import com.autoecole.app.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping(value = "/comptable.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<byte[]> exportComptable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        Long tenantId = TenantContext.getTenantId();
        byte[] content = exportService.genererExportComptableExcel(tenantId, debut, fin);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "export-comptable-" + debut + "_" + fin + ".xlsx");
        return ResponseEntity.ok().headers(headers).body(content);
    }
}
