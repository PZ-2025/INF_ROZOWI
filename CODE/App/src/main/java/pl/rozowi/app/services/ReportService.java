package pl.rozowi.app.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import pl.rozowi.app.models.Team;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Klasa służąca do generowania raportów w formacie PDF.
 * Udostępnia metody do tworzenia raportów dotyczących struktury zespołów,
 * użytkowników systemu oraz przeglądu projektów.
 */
public class ReportService {

    private Font TITLE_FONT;
    private Font SUBTITLE_FONT;
    private Font SECTION_FONT;
    private Font NORMAL_FONT;
    private Font SMALL_FONT;
    private Font TABLE_HEADER_FONT;
    private Font TABLE_DATA_FONT;
    private Font FILTER_FONT;

    private BaseColor PRIMARY_COLOR = new BaseColor(0, 123, 255);
    private BaseColor SECONDARY_COLOR = new BaseColor(30, 30, 47);
    private BaseColor LIGHT_COLOR = new BaseColor(240, 240, 245);

    /**
     * Konstruktor inicjalizujący czcionki używane w raportach.
     * W przypadku błędu ładowania czcionek, używane są domyślne czcionki Helvetica.
     */
    public ReportService() {
        try {
            BaseFont base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            TITLE_FONT = new Font(base, 18, Font.BOLD, PRIMARY_COLOR);
            SUBTITLE_FONT = new Font(base, 14, Font.BOLD, PRIMARY_COLOR);
            SECTION_FONT = new Font(base, 12, Font.BOLD);
            NORMAL_FONT = new Font(base, 10, Font.NORMAL);
            SMALL_FONT = new Font(base, 8, Font.NORMAL);
            TABLE_HEADER_FONT = new Font(base, 10, Font.BOLD, BaseColor.WHITE);
            TABLE_DATA_FONT = new Font(base, 9, Font.NORMAL);
            FILTER_FONT = new Font(base, 10, Font.ITALIC, new BaseColor(100, 100, 100));
        } catch (Exception e) {
            TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, PRIMARY_COLOR);
            SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
            SECTION_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
            TABLE_HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            TABLE_DATA_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            FILTER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, new BaseColor(100, 100, 100));
        }
    }

    /**
     * Generuje raport PDF ze strukturą zespołów.
     *
     * @param filename ścieżka do pliku wynikowego PDF
     * @param content zawartość raportu w formie tekstu
     * @param filterOptions mapa opcji filtrów zastosowanych w raporcie
     * @throws IOException w przypadku problemów z zapisem pliku
     */
    public void generateTeamsStructurePdf(String filename, String content, Map<String, Object> filterOptions) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            doc.open();
            addReportHeader(doc, "Raport: Struktura Zespołów");

            addFilterSection(doc, filterOptions);

            addSeparator(doc);
            parseTeams(doc, content, filterOptions);
        } catch (DocumentException de) {
            throw new IOException(de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    /**
     * Generuje raport PDF z listą użytkowników systemu.
     *
     * @param filename ścieżka do pliku wynikowego PDF
     * @param content zawartość raportu w formie tekstu
     * @param filterOptions mapa opcji filtrów zastosowanych w raporcie
     * @throws IOException w przypadku problemów z zapisem pliku
     */
    public void generateUsersReportPdf(String filename, String content, Map<String, Object> filterOptions) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            doc.open();
            addReportHeader(doc, "Raport: Użytkownicy Systemu");

            addFilterSection(doc, filterOptions);

            addSeparator(doc);
            parseUsers(doc, content, filterOptions);
        } catch (DocumentException de) {
            throw new IOException(de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    /**
     * Generuje raport PDF z przeglądem projektów.
     *
     * @param filename ścieżka do pliku wynikowego PDF
     * @param content zawartość raportu w formie tekstu
     * @param filterOptions mapa opcji filtrów zastosowanych w raporcie
     * @throws IOException w przypadku problemów z zapisem pliku
     */
    public void generateProjectsOverviewPdf(String filename, String content, Map<String, Object> filterOptions) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            doc.open();
            addReportHeader(doc, "Raport: Przegląd Projektów");

            addFilterSection(doc, filterOptions);

            addSeparator(doc);
            parseProjects(doc, content, filterOptions);
        } catch (DocumentException de) {
            throw new IOException(de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    /**
     * Dodaje sekcję z filtrami do dokumentu PDF.
     *
     * @param doc dokument PDF
     * @param filterOptions mapa opcji filtrów do wyświetlenia
     * @throws DocumentException w przypadku problemów z dodaniem zawartości do dokumentu
     */
    private void addFilterSection(Document doc, Map<String, Object> filterOptions) throws DocumentException {
        if (filterOptions == null || filterOptions.isEmpty()) {
            return;
        }

        Paragraph filterHeader = new Paragraph("Zastosowane filtry:", SECTION_FONT);
        filterHeader.setSpacingBefore(5);
        filterHeader.setSpacingAfter(5);
        doc.add(filterHeader);

        String selectedGroup = (String) filterOptions.get("selectedGroup");
        if (selectedGroup != null && !selectedGroup.isEmpty()) {
            Paragraph groupPara = new Paragraph("Grupa: " + selectedGroup, FILTER_FONT);
            groupPara.setIndentationLeft(10);
            doc.add(groupPara);
            Paragraph groupNotePara = new Paragraph("Wszyscy wyświetleni użytkownicy należą do grupy: " + selectedGroup, FILTER_FONT);
            groupNotePara.setIndentationLeft(10);
            groupNotePara.setSpacingAfter(5);
            groupNotePara.setSpacingBefore(2);
            doc.add(groupNotePara);
        }

        @SuppressWarnings("unchecked")
        List<Team> selectedTeams = (List<Team>) filterOptions.get("selectedTeams");
        if (selectedTeams != null && !selectedTeams.isEmpty()) {
            StringBuilder teamsList = new StringBuilder();
            for (int i = 0; i < selectedTeams.size(); i++) {
                teamsList.append(selectedTeams.get(i).getTeamName());
                if (i < selectedTeams.size() - 1) teamsList.append(", ");
            }
            Paragraph teamsPara = new Paragraph("Wybrane zespoły: " + teamsList.toString(), FILTER_FONT);
            teamsPara.setIndentationLeft(10);
            doc.add(teamsPara);
        }

        LocalDate startDate = (LocalDate) filterOptions.get("startDate");
        LocalDate endDate = (LocalDate) filterOptions.get("endDate");

        if (startDate != null) {
            Paragraph startDatePara = new Paragraph("Data początkowa: " + startDate.toString(), FILTER_FONT);
            startDatePara.setIndentationLeft(10);
            doc.add(startDatePara);
        }

        if (endDate != null) {
            Paragraph endDatePara = new Paragraph("Data końcowa: " + endDate.toString(), FILTER_FONT);
            endDatePara.setIndentationLeft(10);
            doc.add(endDatePara);
        }

        Boolean showAdmins = (Boolean) filterOptions.get("showAdmins");
        Boolean showManagers = (Boolean) filterOptions.get("showManagers");
        Boolean showTeamLeaders = (Boolean) filterOptions.get("showTeamLeaders");
        Boolean showUsers = (Boolean) filterOptions.get("showUsers");

        if (showAdmins != null || showManagers != null || showTeamLeaders != null || showUsers != null) {
            StringBuilder userTypes = new StringBuilder("Typy użytkowników: ");
            List<String> types = new ArrayList<>();

            if (showAdmins != null && showAdmins) types.add("Administratorzy");
            if (showManagers != null && showManagers) types.add("Kierownicy");
            if (showTeamLeaders != null && showTeamLeaders) types.add("Team liderzy");
            if (showUsers != null && showUsers) types.add("Pracownicy");

            userTypes.append(String.join(", ", types));

            Paragraph userTypesPara = new Paragraph(userTypes.toString(), FILTER_FONT);
            userTypesPara.setIndentationLeft(10);
            doc.add(userTypesPara);
        }

        Boolean showTasks = (Boolean) filterOptions.get("showTasks");
        Boolean showMembers = (Boolean) filterOptions.get("showMembers");
        Boolean showStatistics = (Boolean) filterOptions.get("showStatistics");

        if (showTasks != null) {
            Paragraph tasksPara = new Paragraph("Pokaż zadania: " + (showTasks ? "Tak" : "Nie"), FILTER_FONT);
            tasksPara.setIndentationLeft(10);
            doc.add(tasksPara);
        }

        if (showMembers != null) {
            Paragraph membersPara = new Paragraph("Pokaż członków zespołów: " + (showMembers ? "Tak" : "Nie"), FILTER_FONT);
            membersPara.setIndentationLeft(10);
            doc.add(membersPara);
        }

        if (showStatistics != null) {
            Paragraph statsPara = new Paragraph("Pokaż statystyki: " + (showStatistics ? "Tak" : "Nie"), FILTER_FONT);
            statsPara.setIndentationLeft(10);
            doc.add(statsPara);
        }

        doc.add(new Paragraph(" "));
    }

    /**
     * Dodaje nagłówek raportu do dokumentu PDF.
     *
     * @param doc dokument PDF
     * @param title tytuł raportu
     * @throws DocumentException w przypadku problemów z dodaniem zawartości do dokumentu
     */
    private void addReportHeader(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, TITLE_FONT);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(10);
        doc.add(p);
        String date = "Wygenerowano: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Paragraph d = new Paragraph(date, NORMAL_FONT);
        d.setAlignment(Element.ALIGN_CENTER);
        d.setSpacingAfter(15);
        doc.add(d);
    }

    /**
     * Dodaje separator wizualny do dokumentu PDF.
     *
     * @param doc dokument PDF
     * @throws DocumentException w przypadku problemów z dodaniem zawartości do dokumentu
     */
    private void addSeparator(Document doc) throws DocumentException {
        Chunk line = new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(0.5f, 100, PRIMARY_COLOR, Element.ALIGN_CENTER, -2));
        Paragraph para = new Paragraph();
        para.add(line);
        doc.add(para);
    }

    /**
     * Klasa wewnętrzna obsługująca stopkę w dokumencie PDF.
     */
    private class FooterEvent extends PdfPageEventHelper {

        /**
         * Metoda wywoływana na końcu każdej strony dokumentu.
         *
         * @param writer obiekt PdfWriter
         * @param doc dokument PDF
         */
        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            cb.setLineWidth(0.5f);
            cb.setColorStroke(PRIMARY_COLOR);
            float y = doc.bottom() - 5;
            cb.moveTo(doc.left(), y);
            cb.lineTo(doc.right(), y);
            cb.stroke();
            Phrase f = new Phrase("TaskApp - Strona " + writer.getPageNumber(), SMALL_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, f, (doc.left() + doc.right())/2, doc.bottom()-20, 0);
        }
    }

    /**
     * Parsuje i dodaje do dokumentu informacje o zespołach.
     *
     * @param doc dokument PDF
     * @param content zawartość raportu w formie tekstu
     * @param filterOptions mapa opcji filtrów
     * @throws DocumentException w przypadku problemów z dodaniem zawartości do dokumentu
     */
    private void parseTeams(Document doc, String content, Map<String, Object> filterOptions) throws DocumentException {
        StringTokenizer st = new StringTokenizer(content, "\n");
        while (st.hasMoreTokens() && !st.nextToken().startsWith("=== ZESPÓŁ:")) {}

        if (!st.hasMoreTokens()) {
            doc.add(new Paragraph("Brak zespołów spełniających kryteria raportu.", NORMAL_FONT));
            addReportFooter(doc);
            return;
        }

        st = new StringTokenizer(content, "\n");

        PdfPTable table = null;
        PdfPTable taskTable = null;
        boolean inTaskSection = false;

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (line.startsWith("=== ZESPÓŁ:")) {
                if (table != null) doc.add(table);
                if (taskTable != null) doc.add(taskTable);
                taskTable = null;
                inTaskSection = false;

                Paragraph h = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);
                table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{30, 70});
            } else if (line.trim().isEmpty()) {
                if (table != null) doc.add(table);
                if (taskTable != null) doc.add(taskTable);
                table = null;
                taskTable = null;
                inTaskSection = false;
            } else if (line.startsWith("ZADANIA ZESPOŁU")) {
                if (table != null) doc.add(table);
                table = null;
                inTaskSection = true;

                Paragraph tasksHeader = new Paragraph("Zadania zespołu:", SECTION_FONT);
                tasksHeader.setSpacingBefore(10);
                tasksHeader.setSpacingAfter(5);
                doc.add(tasksHeader);

                taskTable = new PdfPTable(2);
                taskTable.setWidthPercentage(100);
                taskTable.setWidths(new float[]{50, 50});

                PdfPCell headerCell1 = new PdfPCell(new Phrase("Nazwa zadania", TABLE_HEADER_FONT));
                headerCell1.setBackgroundColor(PRIMARY_COLOR);
                headerCell1.setPadding(5);

                PdfPCell headerCell2 = new PdfPCell(new Phrase("Status i priorytet", TABLE_HEADER_FONT));
                headerCell2.setBackgroundColor(PRIMARY_COLOR);
                headerCell2.setPadding(5);

                taskTable.addCell(headerCell1);
                taskTable.addCell(headerCell2);
            } else if (line.startsWith("- ") && inTaskSection) {
                String taskLine = line.substring(2).trim();

                // Parsowanie informacji o zadaniu
                String taskName = taskLine;
                String taskStatus = "";
                String taskPriority = "";

                int statusIdx = taskLine.indexOf("(Status: ");
                int priorityIdx = taskLine.indexOf(", Priorytet: ");

                if (statusIdx != -1 && priorityIdx != -1) {
                    taskName = taskLine.substring(0, statusIdx).trim();
                    taskStatus = taskLine.substring(statusIdx + 9, priorityIdx).trim();
                    taskPriority = taskLine.substring(priorityIdx + 12, taskLine.length() - 1).trim();

                    PdfPCell nameCell = new PdfPCell(new Phrase(taskName, TABLE_DATA_FONT));
                    nameCell.setPadding(3);

                    PdfPCell infoCell = new PdfPCell(new Phrase("Status: " + taskStatus + ", Priorytet: " + taskPriority, TABLE_DATA_FONT));
                    infoCell.setPadding(3);

                    taskTable.addCell(nameCell);
                    taskTable.addCell(infoCell);
                } else {
                    PdfPCell taskCell = new PdfPCell(new Phrase(taskLine, TABLE_DATA_FONT));
                    taskCell.setPadding(3);
                    taskCell.setColspan(2);
                    taskTable.addCell(taskCell);
                }
            } else if (line.startsWith("CZŁONKOWIE ZESPOŁU")) {
                if (table != null) doc.add(table);
                table = null;

                Paragraph membersHeader = new Paragraph("Członkowie zespołu:", SECTION_FONT);
                membersHeader.setSpacingBefore(10);
                membersHeader.setSpacingAfter(5);
                doc.add(membersHeader);
            } else if (table != null && line.contains(":")) {
                String[] p = line.split(":", 2);
                PdfPCell k = new PdfPCell(new Phrase(p[0].trim() + ":", NORMAL_FONT));
                k.setBorder(Rectangle.NO_BORDER);
                PdfPCell v = new PdfPCell(new Phrase(p[1].trim(), NORMAL_FONT));
                v.setBorder(Rectangle.NO_BORDER);
                table.addCell(k);
                table.addCell(v);
            } else if (line.startsWith("- ") && !inTaskSection) {
                Paragraph pl = new Paragraph(line.substring(1).trim(), NORMAL_FONT);
                pl.setIndentationLeft(10);
                doc.add(pl);
            }
        }

        if (table != null) doc.add(table);
        if (taskTable != null) doc.add(taskTable);

        addReportFooter(doc);
    }

    /**
     * Parsuje i dodaje do dokumentu informacje o użytkownikach.
     *
     * @param doc dokument PDF
     * @param content zawartość raportu w formie tekstu
     * @param filterOptions mapa opcji filtrów
     * @throws DocumentException w przypadku problemów z dodaniem zawartości do dokumentu
     */
    private void parseUsers(Document doc, String content, Map<String, Object> filterOptions) throws DocumentException {
        StringTokenizer st = new StringTokenizer(content, "\n");
        boolean skipFilters = true;
        boolean inTeamLeaderSection = false;
        String selectedGroup = (String) filterOptions.get("selectedGroup");

        while (st.hasMoreTokens() && skipFilters) {
            String line = st.nextToken();
            if (line.startsWith("=== ROLA:")) {
                if (line.contains("Team Lider")) {
                    inTeamLeaderSection = true;
                } else {
                    inTeamLeaderSection = false;
                }

                skipFilters = false;

                String role = line.replace("=== ROLA:", "").replace("===", "").trim();
                Paragraph h = new Paragraph(role, SUBTITLE_FONT);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);

                PdfPTable table;
                String[] hdr;
                float[] widths;

                if (selectedGroup != null && !selectedGroup.isEmpty()) {
                    table = new PdfPTable(4);
                    widths = new float[]{35, 25, 25, 15};
                    hdr = new String[]{"Użytkownik", "Email", "Zespół", "Grupa"};
                } else {
                    table = new PdfPTable(3);
                    widths = new float[]{40, 30, 30};
                    hdr = new String[]{"Użytkownik", "Email", "Zespół"};
                }

                table.setWidthPercentage(100);
                table.setWidths(widths);

                for (String t : hdr) {
                    PdfPCell c = new PdfPCell(new Phrase(t, TABLE_HEADER_FONT));
                    c.setBackgroundColor(PRIMARY_COLOR);
                    c.setPadding(5);
                    table.addCell(c);
                }

                List<String> userLines = new ArrayList<>();
                boolean processingUser = false;

                while (st.hasMoreTokens()) {
                    line = st.nextToken();

                    if (line.startsWith("=== ROLA:")) {

                        if (!userLines.isEmpty()) {
                            processUserLines(table, userLines, inTeamLeaderSection, selectedGroup);
                            userLines.clear();
                        }

                        doc.add(table);

                        role = line.replace("=== ROLA:", "").replace("===", "").trim();
                        h = new Paragraph(role, SUBTITLE_FONT);
                        h.setSpacingBefore(10);
                        h.setSpacingAfter(5);
                        doc.add(h);

                        if (role.contains("Team Lider")) {
                            inTeamLeaderSection = true;
                        } else {
                            inTeamLeaderSection = false;
                        }

                        if (selectedGroup != null && !selectedGroup.isEmpty()) {
                            table = new PdfPTable(4);
                            table.setWidths(widths);
                        } else {
                            table = new PdfPTable(3);
                            table.setWidths(widths);
                        }
                        table.setWidthPercentage(100);

                        for (String t : hdr) {
                            PdfPCell c = new PdfPCell(new Phrase(t, TABLE_HEADER_FONT));
                            c.setBackgroundColor(PRIMARY_COLOR);
                            c.setPadding(5);
                            table.addCell(c);
                        }
                        processingUser = false;
                    } else if (line.startsWith("-")) {
                        if (!userLines.isEmpty()) {
                            processUserLines(table, userLines, inTeamLeaderSection, selectedGroup);
                            userLines.clear();
                        }
                        userLines.add(line);
                        processingUser = true;
                    } else if (processingUser && (line.trim().startsWith("Zespół:") || line.trim().startsWith("Grupa:"))) {
                        userLines.add(line);
                    } else if (line.trim().isEmpty()) {
                        if (!userLines.isEmpty()) {
                            processUserLines(table, userLines, inTeamLeaderSection, selectedGroup);
                            userLines.clear();
                        }
                        processingUser = false;
                    } else if (processingUser) {
                        userLines.add(line);
                    }
                }

                if (!userLines.isEmpty()) {
                    processUserLines(table, userLines, inTeamLeaderSection, selectedGroup);
                }
                doc.add(table);
                break;
            }
        }

        addReportFooter(doc);
    }

    /**
     * Przetwarza linie z danymi użytkownika i dodaje je do tabeli.
     *
     * @param table tabela PDF
     * @param userLines lista linii z danymi użytkownika
     * @param isTeamLeader czy użytkownik jest team liderem
     * @param selectedGroup wybrana grupa użytkowników
     */
    private void processUserLines(PdfPTable table, List<String> userLines, boolean isTeamLeader, String selectedGroup) {
        if (userLines.isEmpty()) return;

        String userLine = userLines.get(0);
        String u = userLine.substring(1).trim();
        String name = u, email = "";
        int s = u.lastIndexOf("(");
        int e = u.lastIndexOf(")");
        if (s >= 0 && e > s) {
            name = u.substring(0, s).trim();
            email = u.substring(s + 1, e);
        }

        List<String> teams = new ArrayList<>();
        String groupName = null;

        for (int i = 1; i < userLines.size(); i++) {
            String line = userLines.get(i).trim();
            if (line.startsWith("Zespół:")) {
                teams.add(line.replace("Zespół:", "").trim());
                if (!isTeamLeader) {
                    break;
                }
            } else if (line.startsWith("Grupa:")) {
                groupName = line.replace("Grupa:", "").trim();
            }
        }

        PdfPCell n = new PdfPCell(new Phrase(name, TABLE_DATA_FONT));
        n.setPadding(5);
        table.addCell(n);

        PdfPCell em = new PdfPCell(new Phrase(email, TABLE_DATA_FONT));
        em.setPadding(5);
        table.addCell(em);

        if (teams.isEmpty()) {
            PdfPCell tm = new PdfPCell(new Phrase("Brak", TABLE_DATA_FONT));
            tm.setPadding(5);
            table.addCell(tm);
        } else {
            Paragraph teamsP = new Paragraph();
            for (String team : teams) {
                teamsP.add(new Phrase(team + "\n", TABLE_DATA_FONT));
            }
            PdfPCell tm = new PdfPCell(teamsP);
            tm.setPadding(5);
            table.addCell(tm);
        }

        if (selectedGroup != null && !selectedGroup.isEmpty()) {
            PdfPCell groupCell = new PdfPCell(new Phrase(selectedGroup, TABLE_DATA_FONT));
            groupCell.setPadding(5);
            table.addCell(groupCell);
        }
    }

    /**
     * Parsuje i dodaje do dokumentu informacje o projektach.
     *
     * @param doc dokument PDF
     * @param content zawartość raportu w formie tekstu
     * @param filterOptions mapa opcji filtrów
     * @throws DocumentException w przypadku problemów z dodaniem zawartości do dokumentu
     */
    private void parseProjects(Document doc, String content, Map<String, Object> filterOptions) throws DocumentException {
        StringTokenizer st = new StringTokenizer(content, "\n");
        boolean foundProject = false;
        PdfPTable mainTable = null;

        while (st.hasMoreTokens() && !foundProject) {
            String line = st.nextToken();
            if (line.startsWith("=== PROJEKT:")) {
                foundProject = true;
                Paragraph h = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);

                mainTable = new PdfPTable(2);
                mainTable.setWidthPercentage(100);
                mainTable.setWidths(new float[]{30, 70});

                PdfPTable taskTable = null;
                boolean inTaskList = false;

                while (st.hasMoreTokens()) {
                    line = st.nextToken();
                    if (line.startsWith("=== PROJEKT:")) {
                        if (mainTable != null) doc.add(mainTable);
                        if (taskTable != null) doc.add(taskTable);

                        h = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
                        h.setSpacingBefore(10);
                        h.setSpacingAfter(5);
                        doc.add(h);

                        mainTable = new PdfPTable(2);
                        mainTable.setWidthPercentage(100);
                        mainTable.setWidths(new float[]{30, 70});
                        inTaskList = false;
                        taskTable = null;
                    } else if (line.trim().isEmpty()) {
                        if (mainTable != null) doc.add(mainTable);
                        mainTable = null;
                        inTaskList = false;
                    } else if (line.startsWith("- ") && inTaskList) {
                        String taskLine = line.substring(2).trim();

                        // Parsowanie informacji o zadaniu w jednolitym formacie
                        String taskName = taskLine;
                        String taskStatus = "";
                        String taskPriority = "";

                        int statusIdx = taskLine.indexOf("(Status: ");
                        int priorityIdx = taskLine.indexOf(", Priorytet: ");

                        if (statusIdx != -1 && priorityIdx != -1) {
                            taskName = taskLine.substring(0, statusIdx).trim();
                            taskStatus = taskLine.substring(statusIdx + 9, priorityIdx).trim();
                            taskPriority = taskLine.substring(priorityIdx + 12, taskLine.length() - 1).trim();

                            PdfPCell nameCell = new PdfPCell(new Phrase(taskName, TABLE_DATA_FONT));
                            nameCell.setPadding(3);

                            PdfPCell infoCell = new PdfPCell(new Phrase("Status: " + taskStatus + ", Priorytet: " + taskPriority, TABLE_DATA_FONT));
                            infoCell.setPadding(3);

                            taskTable.addCell(nameCell);
                            taskTable.addCell(infoCell);
                        } else {
                            PdfPCell taskCell = new PdfPCell(new Phrase(taskLine, TABLE_DATA_FONT));
                            taskCell.setPadding(3);
                            taskCell.setColspan(2);
                            taskTable.addCell(taskCell);
                        }
                    } else if (mainTable != null && line.contains(":")) {
                        String[] p = line.split(":", 2);
                        String key = p[0].trim();
                        String value = p[1].trim();

                        if (key.equals("Liczba zadań")) {
                            if (mainTable != null) doc.add(mainTable);
                            mainTable = null;
                            inTaskList = true;

                            Paragraph tasksHeader = new Paragraph("Zadania projektu:", SECTION_FONT);
                            tasksHeader.setSpacingBefore(10);
                            tasksHeader.setSpacingAfter(5);
                            doc.add(tasksHeader);

                            taskTable = new PdfPTable(2);
                            taskTable.setWidthPercentage(100);
                            taskTable.setWidths(new float[]{50, 50});

                            PdfPCell headerCell1 = new PdfPCell(new Phrase("Nazwa zadania", TABLE_HEADER_FONT));
                            headerCell1.setBackgroundColor(PRIMARY_COLOR);
                            headerCell1.setPadding(5);

                            PdfPCell headerCell2 = new PdfPCell(new Phrase("Status i priorytet", TABLE_HEADER_FONT));
                            headerCell2.setBackgroundColor(PRIMARY_COLOR);
                            headerCell2.setPadding(5);

                            taskTable.addCell(headerCell1);
                            taskTable.addCell(headerCell2);
                        } else {
                            PdfPCell k = new PdfPCell(new Phrase(key + ":", NORMAL_FONT));
                            k.setBorder(Rectangle.NO_BORDER);
                            PdfPCell v = new PdfPCell(new Phrase(value, NORMAL_FONT));
                            v.setBorder(Rectangle.NO_BORDER);
                            mainTable.addCell(k);
                            mainTable.addCell(v);
                        }
                    } else if (line.startsWith("Zespoły:")) {
                        if (mainTable != null) doc.add(mainTable);
                        mainTable = null;

                        Paragraph mh = new Paragraph("Zespoły projektu:", SECTION_FONT);
                        mh.setSpacingBefore(10);
                        mh.setSpacingAfter(5);
                        doc.add(mh);
                    } else if (line.startsWith("-") && !inTaskList) {
                        Paragraph pl = new Paragraph(line.substring(1).trim(), NORMAL_FONT);
                        pl.setIndentationLeft(10);
                        doc.add(pl);
                    }
                }

                if (mainTable != null) doc.add(mainTable);
                if (taskTable != null) doc.add(taskTable);
                break;
            }
        }

        if (!foundProject) {
            doc.add(new Paragraph("Brak projektów spełniających kryteria raportu.", NORMAL_FONT));
        }

        addReportFooter(doc);
    }

    /**
     * Dodaje stopkę raportu do dokumentu PDF.
     *
     * @param doc dokument PDF
     * @throws DocumentException w przypadku problemów z dodaniem zawartości do dokumentu
     */
    private void addReportFooter(Document doc) throws DocumentException {
        addSeparator(doc);
        Paragraph f = new Paragraph(
                "Ten raport został wygenerowany automatycznie przez system TaskApp.", SMALL_FONT
        );
        f.setAlignment(Element.ALIGN_CENTER);
        f.setSpacingAfter(5);
        doc.add(f);
    }
}