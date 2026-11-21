package com.autoecole.app.services;

import com.autoecole.app.dto.LoginRequest;
import com.autoecole.app.dto.LoginResponse;
import com.autoecole.app.dto.UtilisateurDTO;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public LoginResponse login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Identifiants incorrects"));

        if (!passwordEncoder.matches(request.password(), utilisateur.getPasswordHash())) {
            throw new BadCredentialsException("Identifiants incorrects");
        }

        if (utilisateur.getStatut() == StatutUtilisateur.DESACTIVE) {
            throw new BadCredentialsException("Utilisateur desactive");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(utilisateur.getEmail());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tenant_id", utilisateur.getTenantId());
        extraClaims.put("role", utilisateur.getRole().name());
        extraClaims.put("user_id", utilisateur.getId());

        String token = jwtService.generateToken(extraClaims, userDetails);
        UtilisateurDTO userDTO = mapToDTO(utilisateur);

        return new LoginResponse(token, userDTO);
    }

    public LoginResponse refresh(String token) {
        String username = jwtService.extractUsername(token);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", username));

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(token, userDetails)) {
            throw new BadCredentialsException("Token invalide");
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tenant_id", utilisateur.getTenantId());
        extraClaims.put("role", utilisateur.getRole().name());
        extraClaims.put("user_id", utilisateur.getId());

        String newToken = jwtService.generateToken(extraClaims, userDetails);
        UtilisateurDTO userDTO = mapToDTO(utilisateur);

        return new LoginResponse(newToken, userDTO);
    }

    private UtilisateurDTO mapToDTO(Utilisateur u) {
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
