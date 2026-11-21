package com.autoecole.app.services;

import com.autoecole.app.entites.Paiement;
import com.autoecole.app.repositories.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final PaiementRepository paiementRepository;

    public byte[] genererExportComptableExcel(Long tenantId, LocalDate debut, LocalDate fin) {
        List<Paiement> paiements = paiementRepository.findAllByTenantIdAndDateBetween(tenantId, debut, fin);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Paiements");
            int rowNum = 0;
            Row header = sheet.createRow(rowNum++);
            String[] cols = new String[]{"Id","CandidatId","Date","Montant","Mode","Type","NumeroRecu"};
            for (int i=0;i<cols.length;i++) header.createCell(i).setCellValue(cols[i]);

            for (Paiement p : paiements) {
                Row r = sheet.createRow(rowNum++);
                int c = 0;
                r.createCell(c++).setCellValue(p.getId());
                r.createCell(c++).setCellValue(p.getCandidatId());
                r.createCell(c++).setCellValue(p.getDate().toString());
                Cell montant = r.createCell(c++);
                montant.setCellValue(p.getMontant().doubleValue());
                r.createCell(c++).setCellValue(p.getModePaiement()!=null?p.getModePaiement().name():"");
                r.createCell(c++).setCellValue(p.getType()!=null?p.getType().name():"");
                r.createCell(c++).setCellValue(p.getNumeroRecu()!=null?p.getNumeroRecu():"");
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur generation export Excel", e);
        }
    }
}
