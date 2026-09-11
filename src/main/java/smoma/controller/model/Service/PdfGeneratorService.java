package smoma.controller.model.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import smoma.controller.model.EtapeMission;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.MissionOrder;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.Personnel;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PdfGeneratorService {

    private Image loadArtLogo() {
        try {
            ClassPathResource res = new ClassPathResource("static/images/ART logo.jpg");
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    Image img = Image.getInstance(bytes);
                    img.scaleToFit(65, 65);
                    return img;
                }
            }
        } catch (Exception ignored) {}
        try {
            java.io.File file = new java.io.File("src/main/resources/static/images/ART logo.jpg");
            if (file.exists()) {
                Image img = Image.getInstance(file.getAbsolutePath());
                img.scaleToFit(65, 65);
                return img;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public ByteArrayInputStream generateOrdreDeMissionPdf(OrdreDeMission om) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFontFr = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(11, 37, 69));
            Font headerFontEn = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(11, 37, 69));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font stampFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.RED);

            // Official Cameroon 3-Column Header Table
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{42f, 16f, 42f});

            PdfPCell leftCell = new PdfPCell(new Phrase(
                    "RÉPUBLIQUE DU CAMEROUN\nPaix - Travail - Patrie\n---------------\nAGENCE DE RÉGULATION\nDES TÉLÉCOMMUNICATIONS\nDirection Générale", 
                    headerFontFr));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Image logo = loadArtLogo();
            PdfPCell centerCell;
            if (logo != null) {
                centerCell = new PdfPCell(logo, false);
            } else {
                centerCell = new PdfPCell(new Phrase("ART", headerFontFr));
            }
            centerCell.setBorder(Rectangle.NO_BORDER);
            centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            centerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell rightCell = new PdfPCell(new Phrase(
                    "REPUBLIC OF CAMEROON\nPeace - Work - Fatherland\n---------------\nTELECOMMUNICATIONS\nREGULATORY BOARD\nDirectorate General", 
                    headerFontEn));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            headerTable.addCell(leftCell);
            headerTable.addCell(centerCell);
            headerTable.addCell(rightCell);
            document.add(headerTable);

            document.add(new Paragraph(" ", bodyFont));

            // Stamp "SANS FRAIS" if applicable
            if (om.isSansFrais()) {
                PdfPTable stampTable = new PdfPTable(1);
                stampTable.setWidthPercentage(45);
                PdfPCell stampCell = new PdfPCell(new Phrase("SCEAU : SANS FRAIS / WITHOUT EXPENSES", stampFont));
                stampCell.setBorderColor(Color.RED);
                stampCell.setBorderWidth(2f);
                stampCell.setPadding(6);
                stampCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                stampCell.setBackgroundColor(new Color(255, 235, 235));
                stampTable.addCell(stampCell);
                document.add(stampTable);
                document.add(new Paragraph(" ", bodyFont));
            }

            Paragraph docTitle = new Paragraph("ORDRE DE MISSION INDIVIDUEL / MISSION ORDER\nN° : " + om.getReferenceOrdre(), titleFont);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.setSpacingAfter(12);
            document.add(docTitle);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{35f, 65f});

            addTableRow(table, "Nom & Prénom / Full Name:", om.getPersonnel() != null ? om.getPersonnel().getFullName() : "N/A", boldFont, bodyFont);
            addTableRow(table, "Matricule / Staff ID:", om.getPersonnel() != null ? om.getPersonnel().getMatricule() : "N/A", boldFont, bodyFont);
            addTableRow(table, "Fonction & Grade / Title & Rank:", om.getPersonnel() != null ? (om.getPersonnel().getFonction() + " (Grade " + om.getPersonnel().getGrade() + ")") : "N/A", boldFont, bodyFont);
            addTableRow(table, "Structure / Department:", om.getPersonnel() != null ? om.getPersonnel().getDepartement() : "N/A", boldFont, bodyFont);
            addTableRow(table, "Mandat de Réf. / Mandate Ref:", om.getMandatDeMission() != null ? om.getMandatDeMission().getReferenceMandat() : "Direct", boldFont, bodyFont);

            String justif = om.getReferenceJustification();
            if ((justif == null || justif.isBlank()) && om.getMandatDeMission() != null) {
                justif = om.getMandatDeMission().getReferenceJustification();
            }
            if (justif != null && !justif.isBlank()) {
                addTableRow(table, "Réf. / Article de Justification:", justif, boldFont, bodyFont);
            }
            if (om.getDirectionInitiatrice() != null && !om.getDirectionInitiatrice().isBlank()) {
                addTableRow(table, "Direction Initiatrice / Initiating Directorate:", om.getDirectionInitiatrice(), boldFont, bodyFont);
            }
            
            String typeStr = om.getTypeMission() != null ? om.getTypeMission().name() : "INTERNE";
            addTableRow(table, "Type de Mission / Mission Type:", typeStr.equals("EXTERNE") ? "EXTERNE (International)" : "INTERNE (Cameroun)", boldFont, bodyFont);

            String debut = om.getDateDebut() != null ? om.getDateDebut().toString() : "N/A";
            String fin = om.getDateFin() != null ? om.getDateFin().toString() : "N/A";
            long days = (om.getDateDebut() != null && om.getDateFin() != null) ? ChronoUnit.DAYS.between(om.getDateDebut(), om.getDateFin()) + 1 : 1;
            addTableRow(table, "Période d'Exécution / Period:", debut + " au / to " + fin + " (" + days + " jours / days)", boldFont, bodyFont);

            // Transport mode representation (single, mixed, vehicle, plane)
            String transport = om.getMoyenTransport();
            if (transport == null || transport.isBlank()) {
                if (om.getEtape() != null && om.getEtape().getTransportMode() != null) {
                    transport = om.getEtape().getTransportMode();
                } else if (om.getMandatDeMission() != null && !om.getMandatDeMission().getTransportModes().isEmpty()) {
                    transport = String.join(", ", om.getMandatDeMission().getTransportModes());
                } else {
                    transport = typeStr.equals("EXTERNE") ? "Avion" : "Véhicule de service";
                }
            }
            if ("MIXTE".equalsIgnoreCase(transport) || transport.contains("Avion") && transport.contains("Vehicule")) {
                transport = "Mixte (Véhicule et Avion / Vehicle and Plane)";
            }
            addTableRow(table, "Moyen de Transport / Transport Mode:", transport, boldFont, bodyFont);

            String obj = om.getObjectifsSpecifiques();
            if (obj == null || obj.isBlank()) {
                obj = om.getMandatDeMission() != null ? om.getMandatDeMission().getObjetGeneral() : "Mission officielle ART";
            }
            addTableRow(table, "Objet & Objectifs / Purpose:", obj, boldFont, bodyFont);
            addTableRow(table, "Régime Financier / Financial Terms:", om.isSansFrais() ? "SANS FRAIS DE MISSION (Sans indemnité)" : "AVEC FRAIS DE MISSION (Prise en charge officielle)", boldFont, bodyFont);

            document.add(table);

            // Multi-step Itinerary representation if steps exist
            List<EtapeMission> etapes = (om.getEtapes() != null && !om.getEtapes().isEmpty()) 
                    ? om.getEtapes() 
                    : (om.getMandatDeMission() != null ? om.getMandatDeMission().getEtapes() : null);

            if (etapes != null && !etapes.isEmpty()) {
                Paragraph stepsHeader = new Paragraph("\nÉtapes & Itinéraire de la Mission / Mission Steps & Itinerary:", boldFont);
                stepsHeader.setSpacingAfter(6);
                document.add(stepsHeader);

                PdfPTable stepsTable = new PdfPTable(4);
                stepsTable.setWidthPercentage(100);
                stepsTable.setWidths(new float[]{15f, 35f, 25f, 25f});

                stepsTable.addCell(new PdfPCell(new Phrase("Étape", boldFont)));
                stepsTable.addCell(new PdfPCell(new Phrase("Itinéraire / Lieu", boldFont)));
                stepsTable.addCell(new PdfPCell(new Phrase("Dates", boldFont)));
                stepsTable.addCell(new PdfPCell(new Phrase("Transport", boldFont)));

                int stepNum = 1;
                for (EtapeMission st : etapes) {
                    stepsTable.addCell(new PdfPCell(new Phrase("Étape " + stepNum++, bodyFont)));
                    stepsTable.addCell(new PdfPCell(new Phrase(st.getLieu() != null ? st.getLieu() : "Trajet", bodyFont)));
                    String sDate = (st.getDateDebut() != null ? st.getDateDebut().toString() : "") + 
                                  (st.getDateFin() != null ? " au " + st.getDateFin().toString() : "");
                    stepsTable.addCell(new PdfPCell(new Phrase(sDate, bodyFont)));
                    stepsTable.addCell(new PdfPCell(new Phrase(st.getTransportMode() != null ? st.getTransportMode() : "Non spécifié", bodyFont)));
                }
                document.add(stepsTable);
            }

            // Signatures block
            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);
            signTable.setSpacingBefore(20);

            PdfPCell signAgent = new PdfPCell(new Phrase("Visa du Titulaire / Staff Member:\n\n\n______________________", bodyFont));
            signAgent.setBorder(Rectangle.NO_BORDER);

            PdfPCell signDg = new PdfPCell(new Phrase("Pour le Directeur Général / For the GM:\nLe Signataire Habilité (Cachet officiel & Sceau)\n\n______________________", boldFont));
            signDg.setBorder(Rectangle.NO_BORDER);
            signDg.setHorizontalAlignment(Element.ALIGN_RIGHT);

            signTable.addCell(signAgent);
            signTable.addCell(signDg);
            document.add(signTable);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF for OrdreDeMission", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Official printable Mission Mandate: bilingual Cameroon header (Paix-Travail-Patrie /
     * Peace-Work-Fatherland) around the ART logo, for the DG's hand signature; once the signed
     * scan is imported the "APPROUVÉ — Directeur Général" electronic stamp is shown.
     */
    public ByteArrayInputStream generateMandatDeMissionPdf(MandatDeMission m) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFontFr = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(11, 37, 69));
            Font headerFontEn = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(11, 37, 69));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font stampFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.RED);
            Font approveFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(25, 135, 84));

            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{42f, 16f, 42f});
            PdfPCell leftCell = new PdfPCell(new Phrase(
                    "RÉPUBLIQUE DU CAMEROUN\nPaix - Travail - Patrie\n---------------\nAGENCE DE RÉGULATION\nDES TÉLÉCOMMUNICATIONS\nDirection Générale", headerFontFr));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Image logo = loadArtLogo();
            PdfPCell centerCell = (logo != null) ? new PdfPCell(logo, false) : new PdfPCell(new Phrase("ART", headerFontFr));
            centerCell.setBorder(Rectangle.NO_BORDER);
            centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            centerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            PdfPCell rightCell = new PdfPCell(new Phrase(
                    "REPUBLIC OF CAMEROON\nPeace - Work - Fatherland\n---------------\nTELECOMMUNICATIONS\nREGULATORY BOARD\nDirectorate General", headerFontEn));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerTable.addCell(leftCell);
            headerTable.addCell(centerCell);
            headerTable.addCell(rightCell);
            document.add(headerTable);
            document.add(new Paragraph(" ", bodyFont));

            if (m.isSansFrais()) {
                PdfPTable stampTable = new PdfPTable(1);
                stampTable.setWidthPercentage(45);
                PdfPCell stampCell = new PdfPCell(new Phrase("SCEAU : SANS FRAIS / WITHOUT EXPENSES", stampFont));
                stampCell.setBorderColor(Color.RED);
                stampCell.setBorderWidth(2f);
                stampCell.setPadding(6);
                stampCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                stampCell.setBackgroundColor(new Color(255, 235, 235));
                stampTable.addCell(stampCell);
                document.add(stampTable);
                document.add(new Paragraph(" ", bodyFont));
            }

            Paragraph docTitle = new Paragraph("MANDAT DE MISSION / MISSION MANDATE\nN° : " + safe(m.getReferenceMandat()), titleFont);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.setSpacingAfter(12);
            document.add(docTitle);

            PdfPTable t = new PdfPTable(2);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{35f, 65f});
            addTableRow(t, "Réf. / Article de Justification:", safe(m.getReferenceJustification()), boldFont, bodyFont);
            addTableRow(t, "Direction Initiatrice / Initiating Directorate:", safe(m.getDirectionInitiatrice()), boldFont, bodyFont);
            addTableRow(t, "Motif Réglementaire / Regulatory Justification:", safe(m.getMotifReglementaire()), boldFont, bodyFont);
            String type = m.getTypeMission() != null ? m.getTypeMission().name() : "INTERNE";
            addTableRow(t, "Type de Mission / Mission Type:", type.equals("EXTERNE") ? "EXTERNE (International)" : "INTERNE (Cameroun)", boldFont, bodyFont);
            String modes = m.getTransportModes() != null ? String.join(", ", m.getTransportModes()) : "";
            addTableRow(t, "Moyen(s) de Transport / Transport:", modes.isBlank() ? (type.equals("EXTERNE") ? "Avion" : "Véhicule de service") : modes, boldFont, bodyFont);
            addTableRow(t, "Objet Général / General Purpose:", safe(m.getObjetGeneral()), boldFont, bodyFont);
            addTableRow(t, "Objectifs Spécifiques / Specific Objectives:", safe(m.getObjectifsSpecifiques()), boldFont, bodyFont);
            addTableRow(t, "Période Globale / Overall Period:",
                    (m.getDateDebut() != null ? m.getDateDebut().toString() : "N/A") + " au / to " + (m.getDateFin() != null ? m.getDateFin().toString() : "N/A"), boldFont, bodyFont);
            document.add(t);

            java.util.List<EtapeMission> etapes = m.getEtapes();
            if (etapes != null && !etapes.isEmpty()) {
                Paragraph sh = new Paragraph("\nÉtapes & Itinéraire / Steps & Itinerary:", boldFont);
                sh.setSpacingAfter(6);
                document.add(sh);
                PdfPTable st = new PdfPTable(4);
                st.setWidthPercentage(100);
                st.setWidths(new float[]{12f, 38f, 28f, 22f});
                st.addCell(new PdfPCell(new Phrase("Étape", boldFont)));
                st.addCell(new PdfPCell(new Phrase("Lieu / Location", boldFont)));
                st.addCell(new PdfPCell(new Phrase("Dates", boldFont)));
                st.addCell(new PdfPCell(new Phrase("Transport", boldFont)));
                int n = 1;
                for (EtapeMission e : etapes) {
                    st.addCell(new PdfPCell(new Phrase(String.valueOf(n++), bodyFont)));
                    st.addCell(new PdfPCell(new Phrase(safe(e.getLieu()), bodyFont)));
                    st.addCell(new PdfPCell(new Phrase((e.getDateDebut() != null ? e.getDateDebut().toString() : "") + " au " + (e.getDateFin() != null ? e.getDateFin().toString() : ""), bodyFont)));
                    st.addCell(new PdfPCell(new Phrase(safe(e.getTransportMode()), bodyFont)));
                }
                document.add(st);
            }

            java.util.List<Personnel> team = m.getPersonnelList();
            if (team != null && !team.isEmpty()) {
                Paragraph th = new Paragraph("\nÉquipe Désignée / Designated Team:", boldFont);
                th.setSpacingAfter(6);
                document.add(th);
                PdfPTable tt = new PdfPTable(4);
                tt.setWidthPercentage(100);
                tt.setWidths(new float[]{10f, 40f, 25f, 25f});
                tt.addCell(new PdfPCell(new Phrase("#", boldFont)));
                tt.addCell(new PdfPCell(new Phrase("Nom & Prénom / Full Name", boldFont)));
                tt.addCell(new PdfPCell(new Phrase("Matricule", boldFont)));
                tt.addCell(new PdfPCell(new Phrase("Structure", boldFont)));
                int n = 1;
                for (Personnel p : team) {
                    tt.addCell(new PdfPCell(new Phrase(String.valueOf(n++), bodyFont)));
                    tt.addCell(new PdfPCell(new Phrase(safe(p.getNom()) + " " + safe(p.getPrenom()), bodyFont)));
                    tt.addCell(new PdfPCell(new Phrase(safe(p.getMatricule()), bodyFont)));
                    tt.addCell(new PdfPCell(new Phrase(safe(p.getDepartement()), bodyFont)));
                }
                document.add(tt);
            }

            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);
            signTable.setSpacingBefore(24);
            PdfPCell left = new PdfPCell(new Phrase("Cachet & Signature manuscrite (exemplaire imprimé)\n\n\n______________________________", bodyFont));
            left.setBorder(Rectangle.NO_BORDER);
            PdfPCell right;
            if (m.getScanSignedPath() != null && !m.getScanSignedPath().isBlank()) {
                String d = m.getDateValidation() != null ? m.getDateValidation().toString() : "";
                right = new PdfPCell(new Phrase("Le Directeur Général / The General Manager\n\nAPPROUVÉ / APPROVED — ART\nSigné électroniquement" + (d.isBlank() ? "" : " le " + d), approveFont));
            } else {
                right = new PdfPCell(new Phrase("Le Directeur Général / The General Manager\n\n\n______________________________\n(À signer après impression)", boldFont));
            }
            right.setBorder(Rectangle.NO_BORDER);
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);
            signTable.addCell(left);
            signTable.addCell(right);
            document.add(signTable);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF for MandatDeMission", ex);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public ByteArrayInputStream generateMissionOrderPdf(MissionOrder order) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFontFr = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(11, 37, 69));
            Font headerFontEn = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{42f, 16f, 42f});

            PdfPCell leftCell = new PdfPCell(new Phrase("RÉPUBLIQUE DU CAMEROUN\nPaix - Travail - Patrie\n---------------\nAGENCE DE RÉGULATION\nDES TÉLÉCOMMUNICATIONS", headerFontFr));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Image logo = loadArtLogo();
            PdfPCell centerCell;
            if (logo != null) {
                centerCell = new PdfPCell(logo, false);
            } else {
                centerCell = new PdfPCell(new Phrase("ART", headerFontFr));
            }
            centerCell.setBorder(Rectangle.NO_BORDER);
            centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            centerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPCell rightCell = new PdfPCell(new Phrase("REPUBLIC OF CAMEROON\nPeace - Work - Fatherland\n---------------\nTELECOMMUNICATIONS\nREGULATORY BOARD", headerFontEn));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            headerTable.addCell(leftCell);
            headerTable.addCell(centerCell);
            headerTable.addCell(rightCell);
            document.add(headerTable);

            document.add(new Paragraph(" ", bodyFont));

            Paragraph docTitle = new Paragraph("ORDRE DE MISSION N° " + order.getOrderNumber(), titleFont);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.setSpacingAfter(20);
            document.add(docTitle);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{30f, 70f});

            addTableRow(table, "Date d'émission / Issue Date:", order.getIssueDate().toString(), boldFont, bodyFont);
            addTableRow(table, "Objet de la mission / Title:", order.getMissionRequest().getTitle(), boldFont, bodyFont);
            addTableRow(table, "Destination:", order.getMissionRequest().getDestination(), boldFont, bodyFont);
            addTableRow(table, "Itinéraire / Itinerary:", order.getFormDetail().getItinerary(), boldFont, bodyFont);
            addTableRow(table, "Durée / Duration:", order.getFormDetail().getDurationDays() + " Jours / Days", boldFont, bodyFont);
            addTableRow(table, "Moyen de Transport / Mode:", order.getFormDetail().getTransportMode(), boldFont, bodyFont);
            addTableRow(table, "Budget Alloué / Budget:", order.getFormDetail().getAllocatedBudget() + " FCFA", boldFont, bodyFont);
            addTableRow(table, "Statut / Status:", order.getStatus().name(), boldFont, bodyFont);

            document.add(table);

            Paragraph sign = new Paragraph("\n\nPour le Directeur Général / For the GM,\nLe Responsable RH (Signé & Validé)", boldFont);
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
        cell1.setPadding(6);
        cell1.setBackgroundColor(new Color(245, 245, 245));

        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        cell2.setPadding(6);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}