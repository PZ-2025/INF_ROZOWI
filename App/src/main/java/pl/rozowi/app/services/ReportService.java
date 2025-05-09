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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

    public void generateTeamsStructurePdf(String filename, String content, Map<String, Object> filterOptions) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            doc.open();
            addReportHeader(doc, "Raport: Struktura Zespołów");

            // Dodaj sekcję z zastosowanymi filtrami
            addFilterSection(doc, filterOptions);

            addSeparator(doc);
            parseTeams(doc, content, filterOptions);
        } catch (DocumentException de) {
            throw new IOException(de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    public void generateUsersReportPdf(String filename, String content, Map<String, Object> filterOptions) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            doc.open();
            addReportHeader(doc, "Raport: Użytkownicy Systemu");

            // Dodaj sekcję z zastosowanymi filtrami
            addFilterSection(doc, filterOptions);

            addSeparator(doc);
            parseUsers(doc, content, filterOptions);
        } catch (DocumentException de) {
            throw new IOException(de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    public void generateProjectsOverviewPdf(String filename, String content, Map<String, Object> filterOptions) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            doc.open();
            addReportHeader(doc, "Raport: Przegląd Projektów");

            // Dodaj sekcję z zastosowanymi filtrami
            addFilterSection(doc, filterOptions);

            addSeparator(doc);
            parseProjects(doc, content, filterOptions);
        } catch (DocumentException de) {
            throw new IOException(de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    private void addFilterSection(Document doc, Map<String, Object> filterOptions) throws DocumentException {
        if (filterOptions == null || filterOptions.isEmpty()) {
            return;
        }

        Paragraph filterHeader = new Paragraph("Zastosowane filtry:", SECTION_FONT);
        filterHeader.setSpacingBefore(5);
        filterHeader.setSpacingAfter(5);
        doc.add(filterHeader);

        // Filtry zespołów
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

        // Filtry dat
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

        // Filtry zawartości
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

        doc.add(new Paragraph(" ")); // Dodaj pustą linię po filtrach
    }

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

    private void addSeparator(Document doc) throws DocumentException {
        Chunk line = new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(0.5f, 100, PRIMARY_COLOR, Element.ALIGN_CENTER, -2));
        Paragraph para = new Paragraph();
        para.add(line);
        doc.add(para);
    }

    private class FooterEvent extends PdfPageEventHelper {
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

    // Metoda parsująca raport zespołów
    private void parseTeams(Document doc, String content, Map<String, Object> filterOptions) throws DocumentException {
        StringTokenizer st = new StringTokenizer(content, "\n");
        // Pomiń nagłówek i sekcję filtrów
        while (st.hasMoreTokens() && !st.nextToken().startsWith("=== ZESPÓŁ:")) {}

        if (!st.hasMoreTokens()) {
            doc.add(new Paragraph("Brak zespołów spełniających kryteria raportu.", NORMAL_FONT));
            addReportFooter(doc);
            return;
        }

        // Cofnij tokenizer o jedną linię, aby nie utracić pierwszego zespołu
        // (w prawdziwej implementacji potrzebowalibyśmy dostępu do poprzedniego tokena)

        // Tworzymy nowy tokenizer, aby zacząć od początku
        st = new StringTokenizer(content, "\n");

        // Pomiń nagłówek i filtry, szukając pierwszego zespołu
        PdfPTable table = null;
        while (st.hasMoreTokens()) {
            String tmp = st.nextToken();
            if (tmp.startsWith("=== ZESPÓŁ:")) {
                // Przetwórz pierwszy nagłówek
                Paragraph h = new Paragraph(tmp.replace("===", "").trim(), SUBTITLE_FONT);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);
                table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{30, 70});
                break;
            }
        }

        // Główna pętla dla pozostałych linii
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (line.startsWith("=== ZESPÓŁ:")) {
                if (table != null) doc.add(table);
                Paragraph h = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);
                table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{30, 70});
            } else if (line.trim().isEmpty()) {
                if (table != null) doc.add(table);
                table = null;
            } else if (table != null && line.contains(":")) {
                String[] p = line.split(":", 2);
                PdfPCell k = new PdfPCell(new Phrase(p[0].trim() + ":", NORMAL_FONT));
                k.setBorder(Rectangle.NO_BORDER);
                PdfPCell v = new PdfPCell(new Phrase(p[1].trim(), NORMAL_FONT));
                v.setBorder(Rectangle.NO_BORDER);
                table.addCell(k);
                table.addCell(v);
            } else {
                doc.add(new Paragraph(line, NORMAL_FONT));
            }
        }
        if (table != null) doc.add(table);
        addReportFooter(doc);
    }

    // Metoda parsująca raport użytkowników
    private void parseUsers(Document doc, String content, Map<String, Object> filterOptions) throws DocumentException {
        StringTokenizer st = new StringTokenizer(content, "\n");
        // Pomiń nagłówek i sekcję filtrów
        boolean skipFilters = true;
        while (st.hasMoreTokens() && skipFilters) {
            String line = st.nextToken();
            if (line.startsWith("=== ROLA:")) {
                skipFilters = false;

                // Dodaj pierwszy nagłówek roli
                String role = line.replace("=== ROLA:", "").replace("===", "").trim();
                Paragraph h = new Paragraph(role, SUBTITLE_FONT);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);

                // Utwórz tabelę dla użytkowników
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{40, 30, 30});
                String[] hdr = {"Użytkownik", "Email", "Zespół"};
                for (String t : hdr) {
                    PdfPCell c = new PdfPCell(new Phrase(t, TABLE_HEADER_FONT));
                    c.setBackgroundColor(PRIMARY_COLOR);
                    c.setPadding(5);
                    table.addCell(c);
                }

                // Przetwarzaj użytkowników
                while (st.hasMoreTokens()) {
                    line = st.nextToken();
                    if (line.startsWith("=== ROLA:")) {
                        // Zapisz poprzednią tabelę i przejdź do nowej roli
                        doc.add(table);

                        role = line.replace("=== ROLA:", "").replace("===", "").trim();
                        h = new Paragraph(role, SUBTITLE_FONT);
                        h.setSpacingBefore(10);
                        h.setSpacingAfter(5);
                        doc.add(h);

                        table = new PdfPTable(3);
                        table.setWidthPercentage(100);
                        table.setWidths(new float[]{40, 30, 30});
                        for (String t : hdr) {
                            PdfPCell c = new PdfPCell(new Phrase(t, TABLE_HEADER_FONT));
                            c.setBackgroundColor(PRIMARY_COLOR);
                            c.setPadding(5);
                            table.addCell(c);
                        }
                    } else if (line.startsWith("-")) {
                        String u = line.substring(1).trim();
                        String name = u, email = "";
                        int s = u.lastIndexOf("(");
                        int e = u.lastIndexOf(")");
                        if (s >= 0 && e > s) {
                            name = u.substring(0, s).trim();
                            email = u.substring(s + 1, e);
                        }
                        String team = "Brak";
                        if (st.hasMoreTokens()) {
                            String l2 = st.nextToken();
                            if (l2.trim().startsWith("Zespół:")) {
                                team = l2.trim().replace("Zespół:", "").trim();
                            }
                        }

                        PdfPCell n = new PdfPCell(new Phrase(name, TABLE_DATA_FONT));
                        n.setPadding(5);
                        table.addCell(n);
                        PdfPCell em = new PdfPCell(new Phrase(email, TABLE_DATA_FONT));
                        em.setPadding(5);
                        table.addCell(em);
                        PdfPCell tm = new PdfPCell(new Phrase(team, TABLE_DATA_FONT));
                        tm.setPadding(5);
                        table.addCell(tm);
                    }
                }

                // Dodaj ostatnią tabelę
                doc.add(table);
                break;
            }
        }

        addReportFooter(doc);
    }

    // Metoda parsująca przegląd projektów
    private void parseProjects(Document doc, String content, Map<String, Object> filterOptions) throws DocumentException {
        StringTokenizer st = new StringTokenizer(content, "\n");
        // Pomiń nagłówek i sekcję filtrów
        boolean foundProject = false;
        while (st.hasMoreTokens() && !foundProject) {
            String line = st.nextToken();
            if (line.startsWith("=== PROJEKT:")) {
                foundProject = true;
                // Dodaj pierwszy nagłówek projektu
                Paragraph h = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);

                // Utwórz tabelę dla szczegółów projektu
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{30, 70});

                // Przetwarzaj projekty
                while (st.hasMoreTokens()) {
                    line = st.nextToken();
                    if (line.startsWith("=== PROJEKT:")) {
                        // Zapisz poprzednią tabelę i przejdź do nowego projektu
                        if (table != null) doc.add(table);

                        h = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
                        h.setSpacingBefore(10);
                        h.setSpacingAfter(5);
                        doc.add(h);

                        table = new PdfPTable(2);
                        table.setWidthPercentage(100);
                        table.setWidths(new float[]{30, 70});
                    } else if (line.trim().isEmpty()) {
                        if (table != null) doc.add(table);
                        table = null;
                    } else if (table != null && line.contains(":")) {
                        String[] p = line.split(":", 2);
                        PdfPCell k = new PdfPCell(new Phrase(p[0].trim() + ":", NORMAL_FONT));
                        k.setBorder(Rectangle.NO_BORDER);
                        PdfPCell v = new PdfPCell(new Phrase(p[1].trim(), NORMAL_FONT));
                        v.setBorder(Rectangle.NO_BORDER);
                        table.addCell(k);
                        table.addCell(v);
                    } else if (line.startsWith("Zespoły:")) {
                        if (table != null) doc.add(table);
                        table = null;
                        Paragraph mh = new Paragraph("Zespoły projektu:", SECTION_FONT);
                        mh.setSpacingBefore(10);
                        mh.setSpacingAfter(5);
                        doc.add(mh);
                    } else if (line.startsWith("-")) {
                        Paragraph pl = new Paragraph(line.substring(1).trim(), NORMAL_FONT);
                        pl.setIndentationLeft(10);
                        doc.add(pl);
                    }
                }

                // Dodaj ostatnią tabelę
                if (table != null) doc.add(table);
                break;
            }
        }

        // Jeśli nie znaleziono żadnego projektu
        if (!foundProject) {
            doc.add(new Paragraph("Brak projektów spełniających kryteria raportu.", NORMAL_FONT));
        }

        addReportFooter(doc);
    }

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