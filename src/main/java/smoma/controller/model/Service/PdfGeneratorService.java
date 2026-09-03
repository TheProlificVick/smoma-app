package smoma.controller.model.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.MissionOrder;
import smoma.controller.model.OrdreDeMission;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class PdfGeneratorService {

    public ByteArrayInputStream generateOrdreDeMissionPdf(OrdreDeMission om) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFontFr = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(11, 37, 69));
            Font headerFontEn = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(11, 37, 69));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font stampFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.RED);

            // ART Official Header Table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);

            PdfPCell leftCell = new PdfPCell(new Phrase("REPUBLIQUE DU CAMEROUN\nPaix - Travail - Patrie\n---------------\nAGENCE DE REGULATION DES TELECOMMUNICATIONS\nDELEGATION REGIONALE CENTRE-SUD-EST", headerFontFr));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell rightCell = new PdfPCell(new Phrase("REPUBLIC OF CAMEROON\nPeace - Work - Fatherland\n---------------\nTELECOMMUNICATIONS REGULATORY BOARD\nREGIONAL DELEGATION CENTER-SOUTH-EAST", headerFontEn));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            headerTable.addCell(leftCell);
            headerTable.addCell(rightCell);
            document.add(headerTable);

            document.add(new Paragraph(" ", bodyFont));

            // Stamp "SANS FRAIS" if applicable
            if (om.isSansFrais()) {
                PdfPTable stampTable = new PdfPTable(1);
                stampTable.setWidthPercentage(40);
                PdfPCell stampCell = new PdfPCell(new Phrase("SCEAU : SANS FRAIS", stampFont));
                stampCell.setBorderColor(Color.RED);
                stampCell.setBorderWidth(2f);
                stampCell.setPadding(8);
                stampCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                stampCell.setBackgroundColor(new Color(255, 230, 230));
                stampTable.addCell(stampCell);
                document.add(stampTable);
                document.add(new Paragraph(" ", bodyFont));
            }

            Paragraph docTitle = new Paragraph("ORDRE DE MISSION INDIVIDUEL\nRéf : " + om.getReferenceOrdre(), titleFont);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.setSpacingAfter(15);
            document.add(docTitle);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{35f, 65f});

            addTableRow(table, "Nom & Prénom de l'Agent:", om.getPersonnel() != null ? om.getPersonnel().getFullName() : "N/A", boldFont, bodyFont);
            addTableRow(table, "Matricule:", om.getPersonnel() != null ? om.getPersonnel().getMatricule() : "N/A", boldFont, bodyFont);
            addTableRow(table, "Fonction & Grade:", om.getPersonnel() != null ? (om.getPersonnel().getFonction() + " (Grade " + om.getPersonnel().getGrade() + ")") : "N/A", boldFont, bodyFont);
            addTableRow(table, "Structure / Direction:", om.getPersonnel() != null ? om.getPersonnel().getDepartement() : "N/A", boldFont, bodyFont);
            addTableRow(table, "Mandat de Réf. DG:", om.getMandatDeMission() != null ? om.getMandatDeMission().getReferenceMandat() : "Direct", boldFont, bodyFont);
            addTableRow(table, "Type de Mission:", om.getTypeMission() != null ? om.getTypeMission().name() : "INTERNE", boldFont, bodyFont);
            addTableRow(table, "Date de Début:", om.getDateDebut() != null ? om.getDateDebut().toString() : "N/A", boldFont, bodyFont);
            addTableRow(table, "Date de Fin:", om.getDateFin() != null ? om.getDateFin().toString() : "N/A", boldFont, bodyFont);
            addTableRow(table, "Lieu / Etape:", om.getEtape() != null ? om.getEtape().getLieu() : "Etape Principale", boldFont, bodyFont);
            addTableRow(table, "Transport:", om.getEtape() != null && om.getEtape().getTransportMode() != null ? om.getEtape().getTransportMode() : "Vehicule / Avion", boldFont, bodyFont);
            addTableRow(table, "Régime Financier:", om.isSansFrais() ? "SANS FRAIS DE MISSION" : "AVEC PRISE EN CHARGE ET INDEMNITES", boldFont, bodyFont);

            document.add(table);

            Paragraph sign = new Paragraph("\n\nLe Directeur Général / Signataire Habilité\n(Signé électroniquement & Scellé)", boldFont);
            sign.setAlignment(Element.ALIGN_RIGHT);
            document.add(sign);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF for OrdreDeMission", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateMissionOrderPdf(MissionOrder order) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(11, 37, 69));
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);

            Paragraph header = new Paragraph("AGENCE DE REGULATION DES TELECOMMUNICATIONS\n(ART CAMEROUN)", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Direction Générale - Service des Ressources Humaines", subHeaderFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(20);
            document.add(subHeader);

            Paragraph docTitle = new Paragraph("ORDRE DE MISSION N° " + order.getOrderNumber(), titleFont);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.setSpacingAfter(25);
            document.add(docTitle);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{30f, 70f});

            addTableRow(table, "Date d'émission:", order.getIssueDate().toString(), boldFont, bodyFont);
            addTableRow(table, "Objet de la mission:", order.getMissionRequest().getTitle(), boldFont, bodyFont);
            addTableRow(table, "Destination:", order.getMissionRequest().getDestination(), boldFont, bodyFont);
            addTableRow(table, "Itinéraire:", order.getFormDetail().getItinerary(), boldFont, bodyFont);
            addTableRow(table, "Durée:", order.getFormDetail().getDurationDays() + " Jours", boldFont, bodyFont);
            addTableRow(table, "Moyen de Transport:", order.getFormDetail().getTransportMode(), boldFont, bodyFont);
            addTableRow(table, "Budget Alloué:", order.getFormDetail().getAllocatedBudget() + " FCFA", boldFont, bodyFont);
            addTableRow(table, "Statut:", order.getStatus().name(), boldFont, bodyFont);

            document.add(table);

            Paragraph sign = new Paragraph("\n\nPour le Directeur Général,\nLe Responsable RH (Signé & Validé)", boldFont);
            sign.setAlignment(Element.ALIGN_RIGHT);
            document.add(sign);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
        cell1.setPadding(8);
        cell1.setBackgroundColor(new Color(240, 240, 240));

        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        cell2.setPadding(8);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}