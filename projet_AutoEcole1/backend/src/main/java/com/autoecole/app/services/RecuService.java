package com.autoecole.app.services;

import com.autoecole.app.entites.Paiement;
import com.autoecole.app.exception.ResourceNotFoundException;
import com.autoecole.app.repositories.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

@Service
@RequiredArgsConstructor
public class RecuService {

    private final PaiementRepository paiementRepository;

    public byte[] genererRecuPdf(Long tenantId, Long paiementId) {
        Paiement p = paiementRepository.findByTenantIdAndId(tenantId, paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", paiementId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Recu de paiement"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Id paiement: " + p.getId()));
            document.add(new Paragraph("Date: " + p.getDate()));
            document.add(new Paragraph("Montant: " + p.getMontant() + " DH"));
            document.add(new Paragraph("Mode: " + (p.getModePaiement() != null ? p.getModePaiement().name() : "-")));
            document.add(new Paragraph("Type: " + (p.getType() != null ? p.getType().name() : "-")));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Merci pour votre paiement."));

            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur generation PDF recu", e);
        }
    }
}
