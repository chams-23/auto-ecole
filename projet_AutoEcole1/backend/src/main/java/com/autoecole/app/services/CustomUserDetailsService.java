package com.autoecole.app.services;

import com.autoecole.app.entites.Utilisateur;
import com.autoecole.app.enums.StatutUtilisateur;
import com.autoecole.app.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouve avec l'email: " + username));

        if (utilisateur.getStatut() == StatutUtilisateur.DESACTIVE) {
            throw new UsernameNotFoundException("Utilisateur desactive");
        }

        return new User(
                utilisateur.getEmail(),
                utilisateur.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(utilisateur.getRole().name()))
        );
    }
}
