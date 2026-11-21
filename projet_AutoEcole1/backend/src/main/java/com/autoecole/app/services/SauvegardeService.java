package com.autoecole.app.services;

import com.autoecole.app.entites.Sauvegarde;
import com.autoecole.app.repositories.SauvegardeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SauvegardeService {

    private final SauvegardeRepository sauvegardeRepository;

    @Transactional
    public Sauvegarde creerSauvegarde(Long tenantId, String type, String declencheur) {
        try {
            String folder = "backend/backups";
            File dir = new File(folder);
            if (!dir.exists()) dir.mkdirs();
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = "sauvegarde-" + tenantId + "-" + stamp + ".json";
            File out = new File(dir, filename);
            try (FileWriter fw = new FileWriter(out)) {
                fw.write("{\n  \"tenantId\": " + tenantId + ",\n  \"date\": \"" + LocalDateTime.now() + "\"\n}");
            }
            Sauvegarde s = Sauvegarde.builder()
                    .tenantId(tenantId)
                    .typeSauvegarde(type)
                    .nomFichier(filename)
                    .cheminFichier(out.getAbsolutePath())
                    .tailleOctets(out.length())
                    .statut("TERMINE")
                    .declencheur(declencheur)
                    .dateDebut(LocalDateTime.now())
                    .dateFin(LocalDateTime.now())
                    .build();
            return sauvegardeRepository.save(s);
        } catch (Exception e) {
            Sauvegarde s = Sauvegarde.builder()
                    .tenantId(tenantId)
                    .typeSauvegarde(type)
                    .nomFichier("")
                    .cheminFichier("")
                    .statut("ERREUR")
                    .erreurMessage(e.getMessage())
                    .dateDebut(LocalDateTime.now())
                    .build();
            return sauvegardeRepository.save(s);
        }
    }
}
