package smoma.controller.model.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import smoma.controller.model.AvanceSurFrais;
import smoma.controller.model.EtapeMission;
import smoma.controller.model.Genre;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.Personnel;
import smoma.controller.model.Rang;
import smoma.repository.AvanceSurFraisRepository;
import smoma.repository.RangRepository;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PdfGeneratorService {

    private final RangRepository rangRepository;
    private final AvanceSurFraisRepository avanceRepository;
    private final IndemniteService indemniteService;

    public PdfGeneratorService(RangRepository rangRepository, AvanceSurFraisRepository avanceRepository,
                               IndemniteService indemniteService) {
        this.rangRepository = rangRepository;
        this.avanceRepository = avanceRepository;
        this.indemniteService = indemniteService;
    }

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

    /**
     * Official individual mission order, reproducing ART's real two-sided paper "ORDRE DE
     * MISSION" form field-for-field: page 1 is the front (identity/mission/payment-mode/rate
     * decompte), page 2 is the back (observations, advance decompte, note de frais). Fields with
     * no data source in the app (visas, CNI, handwritten amounts in words) are left blank exactly
     * as they are on the blank paper form, to be completed by hand once printed.
     */
    public ByteArrayInputStream generateOrdreDeMissionPdf(OrdreDeMission om) {
        Document document = new Document(PageSize.A4, 40, 40, 30, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFontFr = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(11, 37, 69));
            Font headerFontEn = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, Color.DARK_GRAY);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font approveFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(25, 135, 84));

            Personnel p = om.getPersonnel();
            AvanceSurFrais avance = avanceRepository.findByOrdreDeMission(om).orElse(null);
            String typeStr = om.getTypeMission() != null ? om.getTypeMission().name() : "INTERNE";
            LocalDateSafe dates = new LocalDateSafe(om);
            long days = dates.days();
            BigDecimal dailyRate = p != null ? indemniteService.calculateDailyRate(p, typeStr) : BigDecimal.ZERO;
            BigDecimal totalIndemnite = om.getMontantIndemnite() != null ? om.getMontantIndemnite() : dailyRate.multiply(BigDecimal.valueOf(days));

            // ===== PAGE 1 — FRONT =====
            document.add(bilingualHeaderTable(headerFontFr, headerFontEn));
            document.add(new Paragraph(" ", smallFont));

            PdfPTable refDateTable = new PdfPTable(2);
            refDateTable.setWidthPercentage(100);
            PdfPCell refCell = new PdfPCell(new Phrase("N° " + safe(om.getReferenceOrdre()) + " /ART/DG", boldFont));
            refCell.setBorder(Rectangle.NO_BORDER);
            PdfPCell dateCell = new PdfPCell(new Phrase("Yaoundé, le " + (om.getDateEmission() != null ? om.getDateEmission().toString() : ""), bodyFont));
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            refDateTable.addCell(refCell);
            refDateTable.addCell(dateCell);
            document.add(refDateTable);

            if (om.isSansFrais()) {
                Font stampFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED);
                PdfPTable stampTable = new PdfPTable(1);
                stampTable.setWidthPercentage(45);
                PdfPCell stampCell = new PdfPCell(new Phrase("SCEAU : SANS FRAIS / WITHOUT EXPENSES", stampFont));
                stampCell.setBorderColor(Color.RED);
                stampCell.setBorderWidth(2f);
                stampCell.setPadding(5);
                stampCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                stampCell.setBackgroundColor(new Color(255, 235, 235));
                stampTable.addCell(stampCell);
                document.add(stampTable);
            }

            PdfPTable titleBox = new PdfPTable(1);
            titleBox.setWidthPercentage(100);
            titleBox.setSpacingBefore(6);
            titleBox.setSpacingAfter(10);
            PdfPCell titleCell = new PdfPCell(new Phrase("ORDRE DE MISSION", titleFont));
            titleCell.setBackgroundColor(new Color(11, 37, 69));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setPadding(8);
            titleBox.addCell(titleCell);
            document.add(titleBox);

            String genrePrefix = p != null && p.getGenre() == Genre.FEMME ? "Madame " : "Monsieur ";
            String rangLibelle = rangLibelle(p != null ? p.getRang() : null);
            String destination = om.getLieuDestination();
            if ((destination == null || destination.isBlank()) && om.getEtapes() != null && !om.getEtapes().isEmpty()) {
                destination = om.getEtapes().stream().map(EtapeMission::getLieu).filter(l -> l != null && !l.isBlank())
                        .reduce((a, b) -> a + " - " + b).orElse(null);
            }
            String motif = om.getObjectifsSpecifiques();
            if ((motif == null || motif.isBlank()) && om.getMandatDeMission() != null) motif = om.getMandatDeMission().getObjetGeneral();

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{40f, 60f});
            formFieldRow(info, "Noms et Prénoms", "(Name, First name)", p != null ? genrePrefix + p.getFullName() : "N/A", boldFont, italicFont, bodyFont);
            formFieldRow(info, "Grade", "(Rank)", rangLibelle, boldFont, italicFont, bodyFont);
            formFieldRow(info, "Fonction / Service", "(Function/Office)", p != null ? safe(p.getFonction()) : "N/A", boldFont, italicFont, bodyFont);
            formFieldRow(info, "Destination", "", safe(destination), boldFont, italicFont, bodyFont);
            formFieldRow(info, "Motif", "(Purpose of journey)", safe(motif), boldFont, italicFont, bodyFont);
            formFieldRow(info, "Moyen de transport", "(Means of transport)", safe(om.getMoyenTransport()), boldFont, italicFont, bodyFont);
            formFieldRow(info, "Durée", "(Duration)", "Du " + dates.debut() + " au " + dates.fin() + ".", boldFont, italicFont, bodyFont);
            document.add(info);

            Paragraph sigLine;
            if (om.getScanSignedPath() != null && !om.getScanSignedPath().isBlank()) {
                sigLine = new Paragraph("APPROUVÉ / APPROVED — LE DIRECTEUR GÉNÉRAL\nSigné électroniquement" +
                        (om.getDateEmission() != null ? " le " + om.getDateEmission() : ""), approveFont);
            } else {
                sigLine = new Paragraph("SIGNATURE DU DIRECTEUR GENERAL\n_______________________________", boldFont);
            }
            sigLine.setAlignment(Element.ALIGN_RIGHT);
            sigLine.setSpacingBefore(8);
            sigLine.setSpacingAfter(14);
            document.add(sigLine);

            // MODE DE PAIEMENT
            document.add(sectionHeader("MODE DE PAIEMENT", sectionFont));
            boolean especes = avance != null && avance.getModePaiement() == AvanceSurFrais.ModePaiement.ESPECES;
            boolean virement = avance != null && avance.getModePaiement() == AvanceSurFrais.ModePaiement.VIREMENT;
            PdfPTable paiement = new PdfPTable(2);
            paiement.setWidthPercentage(100);
            paiement.setSpacingBefore(4);
            paiement.setSpacingAfter(10);
            paiement.addCell(plainCell("[" + (especes ? "X" : " ") + "] Espèces : Frs CFA", bodyFont));
            paiement.addCell(plainCell("[ ] Chèque Ordinaire", bodyFont));
            paiement.addCell(plainCell("[ ] Devise (1)", bodyFont));
            paiement.addCell(plainCell("[ ] Chèque de voyage (1)", bodyFont));
            document.add(paiement);
            if (virement) {
                Paragraph virementNote = new Paragraph("Réglé par virement bancaire" +
                        (avance.getReferenceVirement() != null ? " — référence : " + avance.getReferenceVirement() : ""), smallFont);
                virementNote.setSpacingAfter(6);
                document.add(virementNote);
            }

            PdfPTable decompte = new PdfPTable(3);
            decompte.setWidthPercentage(100);
            decompte.setSpacingBefore(4);
            decompte.addCell(headerCell("Nombre de jours", boldFont));
            decompte.addCell(headerCell("Taux", boldFont));
            decompte.addCell(headerCell("Décompte", boldFont));
            decompte.addCell(plainCell(String.valueOf(days), bodyFont));
            decompte.addCell(plainCell(fmt(dailyRate) + " FCFA", bodyFont));
            decompte.addCell(plainCell(fmt(totalIndemnite) + " FCFA", bodyFont));
            document.add(decompte);

            Paragraph arrete = new Paragraph("Arrêté le présent décompte à la somme de " + fmt(totalIndemnite) + " FCFA (" + amountFootnote() + ")", bodyFont);
            arrete.setSpacingBefore(10);
            document.add(arrete);
            Paragraph lieuDate = new Paragraph("A Yaoundé, le " + (om.getDateEmission() != null ? om.getDateEmission().toString() : "......................."), bodyFont);
            lieuDate.setSpacingBefore(4);
            lieuDate.setSpacingAfter(10);
            document.add(lieuDate);

            Paragraph footnote1 = new Paragraph("(1) Préciser la nature des devises", smallFont);
            document.add(footnote1);

            // ===== PAGE 2 — BACK =====
            document.newPage();

            document.add(sectionHeader("OBSERVATIONS", sectionFont));
            PdfPTable obs = new PdfPTable(4);
            obs.setWidthPercentage(100);
            obs.setSpacingBefore(4);
            obs.setSpacingAfter(10);
            for (String h : new String[]{"Visa au départ", "Visa à l'arrivée", "Visa au départ", "Visa à l'arrivée"}) {
                obs.addCell(headerCell(h, boldFont));
            }
            for (int i = 0; i < 4; i++) {
                PdfPCell blank = new PdfPCell(new Phrase(" ", bodyFont));
                blank.setMinimumHeight(40);
                obs.addCell(blank);
            }
            document.add(obs);

            Paragraph prolongement = new Paragraph("Durée et raison du prolongement : ...................................................... Visa du Directeur Général : ......................................", bodyFont);
            prolongement.setSpacingAfter(8);
            document.add(prolongement);
            Paragraph finPrecoce = new Paragraph("Durée et raison de fin précoce de la mission : .............................. Visa du Directeur Général : ......................................", bodyFont);
            finPrecoce.setSpacingAfter(14);
            document.add(finPrecoce);

            document.add(sectionHeader("DECOMPTES DES AVANCES - DETAILS OF ADVANCES", sectionFont));
            PdfPTable avancesHead = new PdfPTable(2);
            avancesHead.setWidthPercentage(100);
            avancesHead.setSpacingBefore(4);
            avancesHead.addCell(headerCell("AU DEPART - AT DEPARTURE", boldFont));
            avancesHead.addCell(headerCell("AU RETOUR - ON RETURN", boldFont));
            document.add(avancesHead);

            BigDecimal montantAvance = avance != null && avance.getMontantAvance() != null ? avance.getMontantAvance() : BigDecimal.ZERO;
            BigDecimal montantSolde = om.getMontantSolde() != null ? om.getMontantSolde() : totalIndemnite.subtract(montantAvance);

            PdfPTable indemJour = new PdfPTable(7);
            indemJour.setWidthPercentage(100);
            indemJour.setWidths(new float[]{22f, 13f, 13f, 13f, 13f, 13f, 13f});
            indemJour.addCell(headerCell("Indemnité Journalière", boldFont));
            indemJour.addCell(headerCell("Nombre", boldFont));
            indemJour.addCell(headerCell("Taux", boldFont));
            indemJour.addCell(headerCell("Décompte", boldFont));
            indemJour.addCell(headerCell("Nombre", boldFont));
            indemJour.addCell(headerCell("Taux", boldFont));
            indemJour.addCell(headerCell("Décompte", boldFont));

            indemJour.addCell(plainCell("Normale - Normal", bodyFont));
            indemJour.addCell(plainCell(String.valueOf(days), bodyFont));
            indemJour.addCell(plainCell(fmt(dailyRate), bodyFont));
            indemJour.addCell(plainCell(fmt(totalIndemnite), bodyFont));
            indemJour.addCell(plainCell("", bodyFont));
            indemJour.addCell(plainCell("", bodyFont));
            indemJour.addCell(plainCell("", bodyFont));

            for (String tier : new String[]{"Réduite - Reduced", "Partielle - Partial"}) {
                indemJour.addCell(plainCell(tier, bodyFont));
                for (int i = 0; i < 6; i++) indemJour.addCell(plainCell("", bodyFont));
            }
            document.add(indemJour);

            PdfPTable arretePaye = new PdfPTable(2);
            arretePaye.setWidthPercentage(100);
            arretePaye.setSpacingBefore(6);
            arretePaye.addCell(plainCell("ARRETE A LA SOMME DE " + fmt(totalIndemnite) + " FCFA\nCLOSE AT THE SUM OF", bodyFont));
            arretePaye.addCell(plainCell("PAYE LA SOMME DE " + fmt(montantAvance) + " FCFA\nPAID THE SUM OF", bodyFont));
            document.add(arretePaye);

            Paragraph payeeTitre = new Paragraph("Payée à titre d'avance" + (avance != null && avance.getPourcentageAvance() != null ? " (" + avance.getPourcentageAvance() + "%)" : ""), smallFont);
            payeeTitre.setSpacingBefore(6);
            payeeTitre.setSpacingAfter(10);
            document.add(payeeTitre);

            document.add(sectionHeader("NOTE DE FRAIS", sectionFont));
            PdfPTable noteFrais = new PdfPTable(2);
            noteFrais.setWidthPercentage(100);
            noteFrais.setSpacingBefore(4);
            noteFrais.setSpacingAfter(4);
            addTableRow(noteFrais, "Montant total des frais :", fmt(totalIndemnite) + " FCFA", boldFont, bodyFont);
            document.add(noteFrais);

            PdfPTable noteFrais2 = new PdfPTable(2);
            noteFrais2.setWidthPercentage(100);
            noteFrais2.addCell(headerCell("DECOMPTE DES AVANCES", boldFont));
            noteFrais2.addCell(headerCell("DECOMPTE DU RESTE", boldFont));
            noteFrais2.addCell(plainCell(fmt(montantAvance) + " FCFA", bodyFont));
            noteFrais2.addCell(plainCell(fmt(montantSolde) + " FCFA", bodyFont));
            document.add(noteFrais2);

            PdfPTable acquit = new PdfPTable(2);
            acquit.setWidthPercentage(100);
            acquit.setSpacingBefore(14);
            acquit.addCell(plainCell("Acquit du bénéficiaire\nReçu : ...........................\nCNI N° : ...........................\nDélivrée le : ................. A : .................\n\nSIGNATURE", bodyFont));
            acquit.addCell(plainCell("Acquit du bénéficiaire\nReçu : ...........................\n\n\n\nSIGNATURE", bodyFont));
            document.add(acquit);

            Paragraph motto = new Paragraph("\nRéguler c'est faciliter", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new Color(11, 37, 69)));
            motto.setAlignment(Element.ALIGN_CENTER);
            motto.setSpacingBefore(14);
            document.add(motto);
            Paragraph coords = new Paragraph(
                    "Yaoundé – Cameroun, Boulevard du 20 mai 1972, Immeuble siège ART\n" +
                    "B.P.: 6132  Tél.: (+237) 222 23 03 80 - 222 23 25 30  Fax: (+237) 222 23 37 48\n" +
                    "Site web: www.art.cm - Email: art@cm", smallFont);
            coords.setAlignment(Element.ALIGN_CENTER);
            document.add(coords);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF for OrdreDeMission", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String rangLibelle(String rangCode) {
        if (rangCode == null || rangCode.isBlank()) return "N/A";
        return rangRepository.findByCode(rangCode).map(Rang::getLibelle).orElse(rangCode);
    }

    private static String fmt(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,d", amount.longValue()).replace(',', ' ');
    }

    private static String amountFootnote() {
        return "voir montant en chiffres ci-dessus / see amount in figures above";
    }

    private PdfPTable bilingualHeaderTable(Font headerFontFr, Font headerFontEn) {
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{42f, 16f, 42f});

        PdfPCell leftCell = new PdfPCell(new Phrase(
                "RÉPUBLIQUE DU CAMEROUN\nPaix - Travail - Patrie\n---------------\nAGENCE DE RÉGULATION\nDES TÉLÉCOMMUNICATIONS",
                headerFontFr));
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Image logo = loadArtLogo();
        PdfPCell centerCell = (logo != null) ? new PdfPCell(logo, false) : new PdfPCell(new Phrase("ART", headerFontFr));
        centerCell.setBorder(Rectangle.NO_BORDER);
        centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        centerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell rightCell = new PdfPCell(new Phrase(
                "REPUBLIC OF CAMEROON\nPeace - Work - Fatherland\n---------------\nTELECOMMUNICATIONS\nREGULATORY BOARD",
                headerFontEn));
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        headerTable.addCell(leftCell);
        headerTable.addCell(centerCell);
        headerTable.addCell(rightCell);
        return headerTable;
    }

    /** A form row matching the paper layout: bold French label + italic English translation stacked, value on the right. */
    private void formFieldRow(PdfPTable table, String labelFr, String labelEn, String value, Font labelFont, Font subFont, Font valueFont) {
        Phrase labelPhrase = new Phrase();
        labelPhrase.add(new Chunk(labelFr + " :\n", labelFont));
        if (labelEn != null && !labelEn.isBlank()) labelPhrase.add(new Chunk(labelEn + " :", subFont));
        PdfPCell labelCell = new PdfPCell(labelPhrase);
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null && !value.isBlank() ? value : "N/A", valueFont));
        valueCell.setPadding(5);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private PdfPTable sectionHeader(String title, Font font) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4);
        PdfPCell c = new PdfPCell(new Phrase(title, font));
        c.setBackgroundColor(new Color(11, 37, 69));
        c.setPadding(4);
        t.addCell(c);
        return t;
    }

    private PdfPCell headerCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(new Color(230, 230, 230));
        c.setPadding(4);
        return c;
    }

    private PdfPCell plainCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(4);
        return c;
    }

    /** Small helper bundling the date-formatting/day-count logic used across the front page. */
    private static class LocalDateSafe {
        private final OrdreDeMission om;
        LocalDateSafe(OrdreDeMission om) { this.om = om; }
        String debut() { return om.getDateDebut() != null ? om.getDateDebut().toString() : "......................."; }
        String fin() { return om.getDateFin() != null ? om.getDateFin().toString() : "......................."; }
        long days() {
            if (om.getDateDebut() == null || om.getDateFin() == null) return 1;
            long d = ChronoUnit.DAYS.between(om.getDateDebut(), om.getDateFin()) + 1;
            return d > 0 ? d : 1;
        }
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
                PdfPTable st = new PdfPTable(5);
                st.setWidthPercentage(100);
                st.setWidths(new float[]{8f, 24f, 20f, 14f, 34f});
                st.addCell(new PdfPCell(new Phrase("Étape", boldFont)));
                st.addCell(new PdfPCell(new Phrase("Lieu / Location", boldFont)));
                st.addCell(new PdfPCell(new Phrase("Dates", boldFont)));
                st.addCell(new PdfPCell(new Phrase("Transport", boldFont)));
                st.addCell(new PdfPCell(new Phrase("Agents Affectés / Assigned Staff", boldFont)));
                int n = 1;
                for (EtapeMission e : etapes) {
                    st.addCell(new PdfPCell(new Phrase(String.valueOf(n++), bodyFont)));
                    st.addCell(new PdfPCell(new Phrase(safe(e.getLieu()), bodyFont)));
                    st.addCell(new PdfPCell(new Phrase((e.getDateDebut() != null ? e.getDateDebut().toString() : "") + " au " + (e.getDateFin() != null ? e.getDateFin().toString() : ""), bodyFont)));
                    st.addCell(new PdfPCell(new Phrase(safe(e.getTransportMode()), bodyFont)));
                    java.util.List<Personnel> stepAgents = e.getPersonnelList();
                    String agentsTxt = (stepAgents != null && !stepAgents.isEmpty())
                            ? stepAgents.stream().map(p -> safe(p.getNom()) + " " + safe(p.getPrenom())).reduce((a, b) -> a + ", " + b).orElse("")
                            : "Toute l'équipe / Whole team";
                    st.addCell(new PdfPCell(new Phrase(agentsTxt, bodyFont)));
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