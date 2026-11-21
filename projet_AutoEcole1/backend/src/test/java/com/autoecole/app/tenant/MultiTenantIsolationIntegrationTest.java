package com.autoecole.app.tenant;

import com.autoecole.app.dto.CandidatDTO;
import com.autoecole.app.enums.Role;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.repositories.UtilisateurRepository;
import com.autoecole.app.security.JwtService;
import com.autoecole.app.services.CandidatService;
import com.autoecole.app.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MultiTenantIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UtilisateurRepository utilisateurRepository;

    @MockBean
    private CandidatService candidatService;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @Test
    void jwtTenantIdShouldWinOverForgedHeaderTenantId() throws Exception {
        Long JWT_TENANT_ID = 10L;
        Long FORGED_HEADER_TENANT_ID = 999L;
        String USER_EMAIL = "admin@tenant10.ma";

        Utilisateur fakeUser = Utilisateur.builder()
                .id(1L)
                .tenantId(JWT_TENANT_ID)
                .role(Role.ADMIN)
                .nom("Admin")
                .prenom("T10")
                .email(USER_EMAIL)
                .statut(StatutUtilisateur.ACTIF)
                .passwordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                .build();

        when(utilisateurRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(fakeUser));

        when(candidatService.lister(any())).thenReturn(List.of());

        User userDetails = new User(
                fakeUser.getEmail(),
                fakeUser.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(fakeUser.getRole().name()))
        );

        String token = jwtService.generateToken(
                java.util.Map.of(
                        "tenant_id", JWT_TENANT_ID,
                        "user_id", fakeUser.getId(),
                        "role", fakeUser.getRole().name()
                ),
                userDetails
        );

        mockMvc.perform(get("/api/candidats")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-Id", String.valueOf(FORGED_HEADER_TENANT_ID)))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> tenantCaptor = ArgumentCaptor.forClass(Long.class);
        verify(candidatService).lister(tenantCaptor.capture());

        Long actualTenantUsed = tenantCaptor.getValue();
        assertEquals(JWT_TENANT_ID, actualTenantUsed,
                "Le service candidatService.lister() doit être appelé avec le tenantId du JWT (" + JWT_TENANT_ID
                        + "), pas celui du header X-Tenant-Id falsifié (" + FORGED_HEADER_TENANT_ID + ")");
    }

    @Test
    void withoutJwtShouldUseHeaderTenantIdForUnauthenticatedRouteIfConfigured() throws Exception {
        Long HEADER_TENANT_ID = 5L;
        String USER_EMAIL = "admin@tenant5.ma";

        Utilisateur fakeUser = Utilisateur.builder()
                .id(2L)
                .tenantId(HEADER_TENANT_ID)
                .role(Role.SECRETAIRE)
                .nom("Sec")
                .prenom("T5")
                .email(USER_EMAIL)
                .statut(StatutUtilisateur.ACTIF)
                .passwordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                .build();

        when(utilisateurRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(fakeUser));

        when(candidatService.lister(any())).thenReturn(List.of());

        User userDetails = new User(
                fakeUser.getEmail(),
                fakeUser.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(fakeUser.getRole().name()))
        );

        String token = jwtService.generateToken(
                java.util.Map.of(
                        "tenant_id", HEADER_TENANT_ID,
                        "user_id", fakeUser.getId(),
                        "role", fakeUser.getRole().name()
                ),
                userDetails
        );

        mockMvc.perform(get("/api/candidats")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-Id", String.valueOf(888L)))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> tenantCaptor = ArgumentCaptor.forClass(Long.class);
        verify(candidatService).lister(tenantCaptor.capture());

        assertEquals(HEADER_TENANT_ID, tenantCaptor.getValue(),
                "Le tenant du JWT doit être préféré au header X-Tenant-Id même avec un autre ID dans le header");
    }
}
