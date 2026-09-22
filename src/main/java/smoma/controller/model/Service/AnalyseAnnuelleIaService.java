package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.*;
import smoma.controller.model.MandatDeMission.StatutMandat;
import smoma.controller.model.OrdreDeMission.StatutOrdre;
import smoma.controller.model.RapportMission.StatutRapport;
import smoma.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * "Assistant IA" analysis of the annual mission report (Bilan Annuel).
 *
 * <p>Reads every mission mandate, mission order and mission report on file and produces a short written
 * analysis (summary, findings) plus a list of cross-checks that confirm the figures shown in the annual
 * report agree with the underlying mandates and orders. Rule-based like {@link AssistantIaService}: it
 * needs no external service, is deterministic, and is strictly read-only — nothing is written or changed.
 * Figures are computed with the same definitions as {@code StatistiquesController} so they match the
 * report page.</p>
 */
@Service
public class AnalyseAnnuelleIaService {

    private static final int NEAR_CAP_DAYS = 80;
    private static final int MAX_LISTED = 3;

    private final MandatDeMissionRepository mandatRepository;
    private final OrdreDeMissionRepository ordreRepository;
    private final PersonnelRepository personnelRepository;
    private final RapportMissionRepository rapportRepository;

    public AnalyseAnnuelleIaService(MandatDeMissionRepository mandatRepository,
                                    OrdreDeMissionRepository ordreRepository,
                                    PersonnelRepository personnelRepository,
                                    RapportMissionRepository rapportRepository) {
        this.mandatRepository = mandatRepository;
        this.ordreRepository = ordreRepository;
        this.personnelRepository = personnelRepository;
        this.rapportRepository = rapportRepository;
    }

    public Map<String, Object> analyse(Integer annee, String langParam) {
        final boolean fr = langParam == null || !langParam.toLowerCase(Locale.ROOT).startsWith("en");
        final Locale locale = fr ? Locale.FRANCE : Locale.ENGLISH;
        final LocalDate today = LocalDate.now();

        List<OrdreDeMission> orders = ordreRepository.findAll();
        List<MandatDeMission> mandates = mandatRepository.findAll();
        List<RapportMission> reports = rapportRepository.findAll();
        List<Personnel> staff = personnelRepository.findAll();

        if (annee != null) {
            orders = orders.stream().filter(o -> o.getDateDebut() != null && o.getDateDebut().getYear() == annee).toList();
            mandates = mandates.stream().filter(m -> m.getDateDebut() != null && m.getDateDebut().getYear() == annee).toList();
            Set<Long> orderIds = orders.stream().map(OrdreDeMission::getId).collect(Collectors.toSet());
            reports = reports.stream()
                    .filter(r -> r.getOrdreDeMission() != null && orderIds.contains(r.getOrdreDeMission().getId()))
                    .toList();
        }

        Map<Long, RapportMission> reportByOrder = new HashMap<>();
        for (RapportMission r : reports) {
            if (r.getOrdreDeMission() != null) reportByOrder.put(r.getOrdreDeMission().getId(), r);
        }

        // ---- headline figures (same definitions as the annual report cards / analysis tables) ----
        long totalMandats = mandates.size();
        long mandatsSignes = mandates.stream().filter(MandatDeMission::isValide).count();
        long totalOrdres = orders.size();
        long totalRapports = reports.size();
        long ordresSansFrais = orders.stream().filter(OrdreDeMission::isSansFrais).count();
        long rapportsFlagges = orders.stream().filter(OrdreDeMission::isRapportSoumis).count();

        long totalJours = 0, ordresAvecDates = 0;
        BigDecimal totalIndemnites = BigDecimal.ZERO, totalAvances = BigDecimal.ZERO, totalSoldes = BigDecimal.ZERO;
        Map<String, Long> byGenre = new LinkedHashMap<>();
        byGenre.put("HOMME", 0L);
        byGenre.put("FEMME", 0L);
        Map<String, Long> byDept = new HashMap<>();
        Map<String, Long> byDest = new HashMap<>();
        Map<String, Long> byTransport = new HashMap<>();
        Map<Month, Long> byMonth = new EnumMap<>(Month.class);
        Map<Long, Long> missionsByAgent = new HashMap<>();
        Map<Long, Personnel> agentById = new HashMap<>();
        Map<Long, Map<Integer, Long>> daysByAgentYear = new HashMap<>();

        for (OrdreDeMission o : orders) {
            long days = 0;
            if (o.getDateDebut() != null && o.getDateFin() != null) {
                long span = ChronoUnit.DAYS.between(o.getDateDebut(), o.getDateFin()) + 1;
                if (span > 0) {
                    days = span;
                    totalJours += span;
                    ordresAvecDates++;
                }
            }
            if (o.getMontantIndemnite() != null) totalIndemnites = totalIndemnites.add(o.getMontantIndemnite());
            if (o.getMontantAvance() != null) totalAvances = totalAvances.add(o.getMontantAvance());
            if (o.getMontantSolde() != null) totalSoldes = totalSoldes.add(o.getMontantSolde());

            Personnel p = o.getPersonnel();
            if (p != null) {
                if (p.getGenre() != null && byGenre.containsKey(p.getGenre().name())) byGenre.merge(p.getGenre().name(), 1L, Long::sum);
                if (p.getDepartement() != null && !p.getDepartement().isBlank()) byDept.merge(p.getDepartement(), 1L, Long::sum);
                if (p.getId() != null) {
                    agentById.put(p.getId(), p);
                    missionsByAgent.merge(p.getId(), 1L, Long::sum);
                    if (days > 0) addDaysPerYear(daysByAgentYear.computeIfAbsent(p.getId(), k -> new HashMap<>()), o.getDateDebut(), o.getDateFin());
                }
            }
            if (o.getEtape() != null && o.getEtape().getLieu() != null && !o.getEtape().getLieu().isBlank()) {
                byDest.merge(o.getEtape().getLieu(), 1L, Long::sum);
            }
            if (o.getMoyenTransport() != null && !o.getMoyenTransport().isBlank()) byTransport.merge(o.getMoyenTransport(), 1L, Long::sum);
            if (o.getDateDebut() != null) byMonth.merge(o.getDateDebut().getMonth(), 1L, Long::sum);
        }
        double dureeMoyenne = ordresAvecDates > 0 ? Math.round((double) totalJours / ordresAvecDates * 10) / 10.0 : 0;
        double tauxRapport = totalOrdres > 0 ? Math.round((double) rapportsFlagges / totalOrdres * 1000) / 10.0 : 0;

        Map<String, Object> chiffres = new LinkedHashMap<>();
        chiffres.put("totalMandats", totalMandats);
        chiffres.put("mandatsSignes", mandatsSignes);
        chiffres.put("totalOrdres", totalOrdres);
        chiffres.put("totalRapports", totalRapports);
        chiffres.put("ordresSansFrais", ordresSansFrais);
        chiffres.put("totalJoursMission", totalJours);
        chiffres.put("dureeMoyenneJours", dureeMoyenne);
        chiffres.put("totalIndemnites", totalIndemnites);
        chiffres.put("tauxRapportSoumisPct", tauxRapport);

        List<Map<String, String>> constats = new ArrayList<>();

        // ---- findings ----
        if (totalOrdres == 0) {
            constats.add(item("info", fr
                    ? "Aucun ordre de mission n'est enregistré" + (annee != null ? " pour " + annee : "") + " : il n'y a pas encore d'activité à analyser."
                    : "No mission order is on file" + (annee != null ? " for " + annee : "") + ": there is no activity to analyse yet."));
        } else {
            // activity volume
            constats.add(item("info", fr
                    ? String.format(locale, "%d ordre(s) de mission couvrent %d jour(s) de mission au total, soit %s jour(s) en moyenne par mission.",
                            totalOrdres, totalJours, fmt(dureeMoyenne, locale))
                    : String.format(locale, "%d mission order(s) cover %d mission day(s) in total, an average of %s day(s) per mission.",
                            totalOrdres, totalJours, fmt(dureeMoyenne, locale))));

            // gender, in absolute terms and relative to headcount
            long mh = byGenre.get("HOMME"), mf = byGenre.get("FEMME");
            long effH = staff.stream().filter(p -> p.getGenre() == Genre.HOMME).count();
            long effF = staff.stream().filter(p -> p.getGenre() == Genre.FEMME).count();
            if (mh == mf) {
                constats.add(item("info", fr
                        ? String.format(locale, "Égalité entre hommes et femmes : %d mission(s) chacun.", mh)
                        : String.format(locale, "Men and women are level: %d mission(s) each.", mh)));
            } else {
                boolean menLead = mh > mf;
                String relFr = "", relEn = "";
                if (effH > 0 && effF > 0) {
                    String rh = fmt((double) mh / effH, locale), rf = fmt((double) mf / effF, locale);
                    relFr = String.format(locale, " Rapporté à l'effectif (%d homme(s), %d femme(s)), cela représente %s mission(s) par homme contre %s par femme.", effH, effF, rh, rf);
                    relEn = String.format(locale, " Relative to headcount (%d men, %d women) that is %s mission(s) per man versus %s per woman.", effH, effF, rh, rf);
                }
                constats.add(item("info", fr
                        ? String.format(locale, "%s ont effectué le plus de missions (%d homme(s) contre %d femme(s)).", menLead ? "Les hommes" : "Les femmes", mh, mf) + relFr
                        : String.format(locale, "%s carried out the most missions (%d men versus %d women).", menLead ? "Men" : "Women", mh, mf) + relEn));
            }

            // concentration
            topLine(byDept, totalOrdres, locale, fr,
                    "La structure la plus active est « %s » avec %d mission(s) (%s %% des ordres).",
                    "The most active structure is \"%s\" with %d mission(s) (%s %% of orders).", constats);
            topLine(byDest, totalOrdres, locale, fr,
                    "La destination la plus fréquente est « %s » (%d mission(s), %s %%).",
                    "The most frequent destination is \"%s\" (%d mission(s), %s %%).", constats);
            topLine(byTransport, totalOrdres, locale, fr,
                    "Le moyen de transport dominant est « %s » (%d mission(s), %s %%).",
                    "The dominant means of transport is \"%s\" (%d mission(s), %s %%).", constats);
            byMonth.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(e -> {
                String mois = e.getKey().getDisplayName(TextStyle.FULL, locale);
                constats.add(item("info", fr
                        ? String.format(locale, "Le mois le plus chargé est %s avec %d départ(s) en mission.", mois, e.getValue())
                        : String.format(locale, "The busiest month is %s with %d mission departure(s).", mois, e.getValue())));
            });
            missionsByAgent.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(e -> {
                Personnel p = agentById.get(e.getKey());
                constats.add(item("info", fr
                        ? String.format(locale, "L'agent le plus sollicité est %s avec %d mission(s).", p.getFullName(), e.getValue())
                        : String.format(locale, "The most frequently assigned staff member is %s with %d mission(s).", p.getFullName(), e.getValue())));
            });

            // 100 days / fiscal-year cap (MissionCapacityService)
            int cap = MissionCapacityService.MAX_DAYS_PER_FISCAL_YEAR;
            List<String> over = new ArrayList<>(), near = new ArrayList<>();
            for (Map.Entry<Long, Map<Integer, Long>> a : daysByAgentYear.entrySet()) {
                for (Map.Entry<Integer, Long> y : a.getValue().entrySet()) {
                    String label = agentById.get(a.getKey()).getFullName() + " (" + y.getKey() + " : " + y.getValue() + ")";
                    if (y.getValue() > cap) over.add(label);
                    else if (y.getValue() >= NEAR_CAP_DAYS) near.add(label);
                }
            }
            if (!over.isEmpty()) {
                constats.add(item("warning", fr
                        ? String.format(locale, "%d agent(s) dépassent le plafond réglementaire de %d jours de mission par an : %s.", over.size(), cap, sample(over))
                        : String.format(locale, "%d staff member(s) exceed the regulatory ceiling of %d mission days per year: %s.", over.size(), cap, sample(over))));
            }
            if (!near.isEmpty()) {
                constats.add(item("info", fr
                        ? String.format(locale, "%d agent(s) approchent du plafond annuel de %d jours (au moins %d jours) : %s.", near.size(), cap, NEAR_CAP_DAYS, sample(near))
                        : String.format(locale, "%d staff member(s) are approaching the annual ceiling of %d days (at least %d days): %s.", near.size(), cap, NEAR_CAP_DAYS, sample(near))));
            }

            // reports
            List<String> lateRefs = new ArrayList<>();
            for (OrdreDeMission o : orders) {
                LocalDate end = o.getDateFinReelle() != null ? o.getDateFinReelle() : o.getDateFin();
                boolean over_ = o.isMissionTerminee() || (end != null && end.isBefore(today));
                if (over_ && !reportFiled(o, reportByOrder)) lateRefs.add(o.getReferenceOrdre() != null ? o.getReferenceOrdre() : "#" + o.getId());
            }
            if (lateRefs.isEmpty()) {
                constats.add(item("success", fr
                        ? String.format(locale, "Toutes les missions terminées ont un rapport déposé (taux de dépôt affiché : %s %%).", fmt(tauxRapport, locale))
                        : String.format(locale, "Every completed mission has a report filed (reported filing rate: %s %%).", fmt(tauxRapport, locale))));
            } else {
                constats.add(item("warning", fr
                        ? String.format(locale, "%d mission(s) terminée(s) n'ont toujours pas de rapport déposé : %s. Taux de dépôt affiché : %s %%.", lateRefs.size(), sample(lateRefs), fmt(tauxRapport, locale))
                        : String.format(locale, "%d completed mission(s) still have no report filed: %s. Reported filing rate: %s %%.", lateRefs.size(), sample(lateRefs), fmt(tauxRapport, locale))));
            }

            // money
            BigDecimal moyenne = totalOrdres > 0 ? totalIndemnites.divide(BigDecimal.valueOf(totalOrdres), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            constats.add(item("info", fr
                    ? String.format(locale, "Les indemnités calculées totalisent %s FCFA (moyenne %s FCFA par mission) ; %d mission(s) sont « sans frais ».", money(totalIndemnites, locale), money(moyenne, locale), ordresSansFrais)
                    : String.format(locale, "Calculated indemnities total %s FCFA (average %s FCFA per mission); %d mission(s) are marked \"no expenses\".", money(totalIndemnites, locale), money(moyenne, locale), ordresSansFrais)));
            long fraisSansMontant = orders.stream()
                    .filter(o -> !o.isSansFrais() && (o.getMontantIndemnite() == null || o.getMontantIndemnite().signum() == 0)).count();
            if (fraisSansMontant > 0) {
                constats.add(item("warning", fr
                        ? String.format(locale, "%d mission(s) avec frais n'ont aucune indemnité calculée : le total indemnités du bilan peut être sous-évalué.", fraisSansMontant)
                        : String.format(locale, "%d mission(s) with expenses have no indemnity calculated: the indemnity total in the report may be understated.", fraisSansMontant)));
            }

            // pipeline
            long brouillons = orders.stream().filter(o -> o.getStatut() == StatutOrdre.BROUILLON_MODIFIABLE).count();
            if (brouillons > 0) {
                constats.add(item("info", fr
                        ? String.format(locale, "%d ordre(s) de mission sont encore à l'état de brouillon (non signés).", brouillons)
                        : String.format(locale, "%d mission order(s) are still drafts (not yet signed).", brouillons)));
            }
        }
        long enAttenteSignature = mandates.stream().filter(m -> m.getStatut() == StatutMandat.EN_ATTENTE_SIGNATURE).count();
        if (enAttenteSignature > 0) {
            constats.add(item("info", fr
                    ? String.format(locale, "%d mandat(s) attendent encore la signature du Directeur Général.", enAttenteSignature)
                    : String.format(locale, "%d mandate(s) are still awaiting the General Manager's signature.", enAttenteSignature)));
        }

        // ---- cross-checks: do the report figures agree with the mandates and orders? ----
        List<Map<String, Object>> verifications = new ArrayList<>();

        verifications.add(check(fr ? "Rapports déposés : cartes du bilan et ordres de mission" : "Reports filed: report cards vs mission orders",
                totalRapports == rapportsFlagges,
                fr ? String.format(locale, "%d rapport(s) enregistré(s) ; %d ordre(s) marqué(s) « rapport soumis ».", totalRapports, rapportsFlagges)
                   : String.format(locale, "%d report(s) on file; %d order(s) marked \"report submitted\".", totalRapports, rapportsFlagges)));

        long sansMandat = orders.stream().filter(o -> o.getMandatDeMission() == null).count();
        verifications.add(check(fr ? "Chaque ordre de mission est rattaché à un mandat" : "Every mission order belongs to a mandate",
                sansMandat == 0,
                fr ? String.format(locale, "%d ordre(s) sans mandat sur %d.", sansMandat, totalOrdres)
                   : String.format(locale, "%d order(s) without a mandate out of %d.", sansMandat, totalOrdres)));

        Set<Long> mandatsAvecOrdres = orders.stream().filter(o -> o.getMandatDeMission() != null)
                .map(o -> o.getMandatDeMission().getId()).collect(Collectors.toSet());
        Set<StatutMandat> pending = EnumSet.of(StatutMandat.EN_ATTENTE_SIGNATURE, StatutMandat.EN_ATTENTE, StatutMandat.ANNULE);
        long signesSansOrdre = mandates.stream()
                .filter(m -> m.getStatut() != null && !pending.contains(m.getStatut()) && !mandatsAvecOrdres.contains(m.getId())).count();
        verifications.add(check(fr ? "Chaque mandat signé a généré ses ordres de mission" : "Every signed mandate has generated its mission orders",
                signesSansOrdre == 0,
                fr ? String.format(locale, "%d mandat(s) signé(s) sans aucun ordre de mission.", signesSansOrdre)
                   : String.format(locale, "%d signed mandate(s) with no mission order.", signesSansOrdre)));

        long horsPeriode = orders.stream().filter(o -> {
            MandatDeMission m = o.getMandatDeMission();
            if (m == null || m.getDateDebut() == null || m.getDateFin() == null || o.getDateDebut() == null || o.getDateFin() == null) return false;
            return o.getDateDebut().isBefore(m.getDateDebut()) || o.getDateFin().isAfter(m.getDateFin());
        }).count();
        verifications.add(check(fr ? "Les dates des ordres respectent la période du mandat" : "Order dates stay within the mandate period",
                horsPeriode == 0,
                fr ? String.format(locale, "%d ordre(s) hors de la période de leur mandat.", horsPeriode)
                   : String.format(locale, "%d order(s) outside their mandate's period.", horsPeriode)));

        long horsEquipe = orders.stream().filter(o -> {
            MandatDeMission m = o.getMandatDeMission();
            if (m == null || o.getPersonnel() == null || m.getPersonnelList() == null || m.getPersonnelList().isEmpty()) return false;
            return m.getPersonnelList().stream().noneMatch(p -> Objects.equals(p.getId(), o.getPersonnel().getId()));
        }).count();
        verifications.add(check(fr ? "L'agent de chaque ordre fait partie de l'équipe du mandat" : "Each order's agent belongs to the mandate team",
                horsEquipe == 0,
                fr ? String.format(locale, "%d ordre(s) attribué(s) à un agent hors équipe.", horsEquipe)
                   : String.format(locale, "%d order(s) assigned to an agent outside the team.", horsEquipe)));

        long fraisDivergents = orders.stream()
                .filter(o -> o.getMandatDeMission() != null && o.getMandatDeMission().isSansFrais() != o.isSansFrais()).count();
        verifications.add(check(fr ? "La mention « sans frais » des ordres suit celle du mandat" : "The \"no expenses\" mention on orders follows the mandate",
                fraisDivergents == 0,
                fr ? String.format(locale, "%d ordre(s) divergent de leur mandat.", fraisDivergents)
                   : String.format(locale, "%d order(s) differ from their mandate.", fraisDivergents)));

        long ok = verifications.stream().filter(v -> Boolean.TRUE.equals(v.get("ok"))).count();

        // ---- summary paragraph ----
        long warnings = constats.stream().filter(c -> "warning".equals(c.get("niveau"))).count() + (verifications.size() - ok);
        StringBuilder resume = new StringBuilder();
        if (fr) {
            resume.append(String.format(locale, "Bilan%s : %d mandat(s) (dont %d signé(s)), %d ordre(s) de mission et %d rapport(s) déposé(s), pour %d jour(s) de mission et %s FCFA d'indemnités calculées. ",
                    annee != null ? " " + annee : "", totalMandats, mandatsSignes, totalOrdres, totalRapports, totalJours, money(totalIndemnites, locale)));
            resume.append(String.format(locale, "%d contrôle(s) de cohérence sur %d sont conformes. ", ok, verifications.size()));
            resume.append(warnings == 0 ? "Aucun point d'attention n'a été détecté." : warnings + " point(s) d'attention à examiner ci-dessous.");
        } else {
            resume.append(String.format(locale, "Report%s: %d mandate(s) (%d signed), %d mission order(s) and %d report(s) filed, for %d mission day(s) and %s FCFA of calculated indemnities. ",
                    annee != null ? " " + annee : "", totalMandats, mandatsSignes, totalOrdres, totalRapports, totalJours, money(totalIndemnites, locale)));
            resume.append(String.format(locale, "%d of %d consistency checks pass. ", ok, verifications.size()));
            resume.append(warnings == 0 ? "No point of attention was detected." : warnings + " point(s) of attention to review below.");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("annee", annee);
        result.put("langue", fr ? "fr" : "en");
        result.put("genereLe", new Date());
        result.put("resume", resume.toString());
        result.put("chiffres", chiffres);
        result.put("constats", constats);
        result.put("verifications", verifications);
        result.put("controlesConformes", ok);
        result.put("controlesTotal", verifications.size());
        return result;
    }

    private static boolean reportFiled(OrdreDeMission o, Map<Long, RapportMission> reportByOrder) {
        if (o.isRapportSoumis()) return true;
        RapportMission r = reportByOrder.get(o.getId());
        return r != null && r.getStatut() != null && r.getStatut() != StatutRapport.NON_DEPOSE;
    }

    /** Adds the days of [start, end] to the per-calendar-year tally (a mission spanning a year end counts in both). */
    private static void addDaysPerYear(Map<Integer, Long> tally, LocalDate start, LocalDate end) {
        for (int y = start.getYear(); y <= end.getYear(); y++) {
            LocalDate from = start.isAfter(LocalDate.of(y, 1, 1)) ? start : LocalDate.of(y, 1, 1);
            LocalDate to = end.isBefore(LocalDate.of(y, 12, 31)) ? end : LocalDate.of(y, 12, 31);
            tally.merge(y, ChronoUnit.DAYS.between(from, to) + 1, Long::sum);
        }
    }

    private static void topLine(Map<String, Long> counts, long total, Locale locale, boolean fr,
                                String frTpl, String enTpl, List<Map<String, String>> out) {
        counts.entrySet().stream().max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .ifPresent(e -> out.add(item("info", String.format(locale, fr ? frTpl : enTpl, e.getKey(), e.getValue(),
                        fmt(total > 0 ? (double) e.getValue() / total * 100 : 0, locale)))));
    }

    private static String sample(List<String> values) {
        String head = values.stream().limit(MAX_LISTED).collect(Collectors.joining(", "));
        return values.size() > MAX_LISTED ? head + ", …" : head;
    }

    private static String fmt(double v, Locale locale) {
        return String.format(locale, "%.1f", v);
    }

    private static String money(BigDecimal v, Locale locale) {
        return String.format(locale, "%,d", v.setScale(0, RoundingMode.HALF_UP).longValue());
    }

    private static Map<String, String> item(String niveau, String texte) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("niveau", niveau);
        m.put("texte", texte);
        return m;
    }

    private static Map<String, Object> check(String libelle, boolean ok, String detail) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("libelle", libelle);
        m.put("ok", ok);
        m.put("detail", detail);
        return m;
    }
}
