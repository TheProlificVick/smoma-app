package smoma.controller.model.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import smoma.controller.model.MissionOrder;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class PdfGeneratorService {

    public ByteArrayInputStream generateMissionOrderPdf(MissionOrder order) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLUE);
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