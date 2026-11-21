package com.autoecole.app.controllers;

import com.autoecole.app.dto.CreateCandidatRequest;
import com.autoecole.app.dto.LoginRequest;
import com.autoecole.app.dto.LoginResponse;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.services.AuthService;
import com.autoecole.app.services.CandidatService;
import com.autoecole.app.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final CandidatService candidatService;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurRepository utilisateurRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Inscription publique d'un nouvel élève (candidat).
     * Crée le compte puis retourne un JWT (connexion automatique).
     */
    @PostMapping("/inscription")
    public ResponseEntity<LoginResponse> inscription(@Valid @RequestBody CreateCandidatRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            tenantId = 1L;
        }
        candidatService.creer(request, tenantId);
        LoginResponse response = authService.login(new LoginRequest(request.email(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        LoginResponse response = authService.refresh(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/encode")
    public ResponseEntity<Map<String, String>> encode(@RequestParam String password) {
        Map<String, String> res = new HashMap<>();
        res.put("hash", passwordEncoder.encode(password));
        res.put("password", password);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset-all-passwords")
    public ResponseEntity<Map<String, Object>> resetAllPasswords(@RequestParam(defaultValue = "password123") String password) {
        String hash = passwordEncoder.encode(password);
        List<Utilisateur> all = utilisateurRepository.findAll();
        for (Utilisateur u : all) {
            u.setPasswordHash(hash);
            utilisateurRepository.save(u);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("updated", all.size());
        res.put("password", password);
        res.put("hash", hash);
        return ResponseEntity.ok(res);
    }
}
