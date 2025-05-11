package pl.rozowi.app.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

public class TeamLeaderReportService {

    private Font titleFont;
    private Font subtitleFont;
    private Font sectionFont;
    private Font normalFont;
    private Font smallFont;
    private Font tableHeaderFont;
    private Font tableDataFont;

    private BaseColor primaryColor = new BaseColor(0, 123, 255);

    public TeamLeaderReportService() {
        try {
            BaseFont base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            titleFont = new Font(base, 18, Font.BOLD, primaryColor);
            subtitleFont = new Font(base, 14, Font.BOLD, primaryColor);
            sectionFont = new Font(base, 12, Font.BOLD);
            normalFont = new Font(base, 10, Font.NORMAL);
            smallFont = new Font(base, 8, Font.NORMAL);
            tableHeaderFont = new Font(base, 10, Font.BOLD, BaseColor.WHITE);
            tableDataFont = new Font(base, 9, Font.NORMAL);
        } catch (Exception e) {
            titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, primaryColor);
            subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, primaryColor);
            sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            smallFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
            tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            tableDataFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        }
    }

    public void generateTeamMembersReportPdf(String filename, String content) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            doc.open();
            addReportHeader(doc, "Raport: Członkowie Zespołu");
            addSeparator(doc);
            parseTeamMembers(doc, content);
        } catch (DocumentException de) {
            throw new IOException(de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    public void generateTeamTasksReportPdf(String filename, String content) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
            writer.setPageEvent(new FooterEvent());
            doc.open();
            addReportHeader(doc, "Raport: Zadania Zespołu");
            addSeparator(doc);
            parseTeamTasks(doc, content);
        } catch (DocumentException de) {
            throw new IOException(de.getMessage(), de);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    private void addReportHeader(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, titleFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(10);
        doc.add(p);
        String date = "Wygenerowano: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Paragraph d = new Paragraph(date, normalFont);
        d.setAlignment(Element.ALIGN_CENTER);
        d.setSpacingAfter(15);
        doc.add(d);
    }

    private void addSeparator(Document doc) throws DocumentException {
        Chunk line = new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(0.5f, 100, primaryColor, Element.ALIGN_CENTER, -2));
        Paragraph para = new Paragraph();
        para.add(line);
        doc.add(para);
    }

    private class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            cb.setLineWidth(0.5f);
            cb.setColorStroke(primaryColor);
            float y = doc.bottom() - 5;
            cb.moveTo(doc.left(), y);
            cb.lineTo(doc.right(), y);
            cb.stroke();
            Phrase f = new Phrase("TaskApp - Strona " + writer.getPageNumber(), smallFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, f, (doc.left() + doc.right())/2, doc.bottom()-20, 0);
        }
    }

    private void parseTeamMembers(Document doc, String content) throws DocumentException {
        StringTokenizer st = new StringTokenizer(content, "\n");

        while (st.hasMoreTokens() && !st.nextToken().startsWith("=== ZESPÓŁ:")) {}

        if (!st.hasMoreTokens()) {
            doc.add(new Paragraph("Brak danych o członkach zespołu.", normalFont));
            addReportFooter(doc);
            return;
        }

        st = new StringTokenizer(content, "\n");

        PdfPTable table = null;
        boolean foundTeam = false;

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (line.startsWith("=== ZESPÓŁ:")) {
                foundTeam = true;
                if (table != null) doc.add(table);

                Paragraph h = new Paragraph(line.replace("===", "").trim(), subtitleFont);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);

                table = new PdfPTable(3);
                table.setWidthPercentage(100);
                try {
                    table.setWidths(new float[]{40, 30, 30});
                } catch (DocumentException e) {
                }

                PdfPCell headerName = new PdfPCell(new Phrase("Imię i Nazwisko", tableHeaderFont));
                headerName.setBackgroundColor(primaryColor);
                headerName.setPadding(5);
                table.addCell(headerName);

                PdfPCell headerEmail = new PdfPCell(new Phrase("Email", tableHeaderFont));
                headerEmail.setBackgroundColor(primaryColor);
                headerEmail.setPadding(5);
                table.addCell(headerEmail);

                PdfPCell headerRole = new PdfPCell(new Phrase("Rola", tableHeaderFont));
                headerRole.setBackgroundColor(primaryColor);
                headerRole.setPadding(5);
                table.addCell(headerRole);
            } else if (foundTeam && line.startsWith("- ")) {
                String memberLine = line.substring(2).trim();
                String[] parts = memberLine.split("\\(", 2);
                String name = parts[0].trim();
                String email = parts.length > 1 ? parts[1].replace(")", "").trim() : "";

                PdfPCell nameCell = new PdfPCell(new Phrase(name, tableDataFont));
                nameCell.setPadding(5);
                table.addCell(nameCell);

                PdfPCell emailCell = new PdfPCell(new Phrase(email, tableDataFont));
                emailCell.setPadding(5);
                table.addCell(emailCell);

                PdfPCell roleCell = new PdfPCell(new Phrase("Pracownik", tableDataFont));
                roleCell.setPadding(5);
                table.addCell(roleCell);
            }
        }

        if (table != null) doc.add(table);
        if (!foundTeam) {
            doc.add(new Paragraph("Brak danych o członkach zespołu.", normalFont));
        }

        addReportFooter(doc);
    }

    private void parseTeamTasks(Document doc, String content) throws DocumentException {
        StringTokenizer st = new StringTokenizer(content, "\n");

        while (st.hasMoreTokens() && !st.nextToken().startsWith("=== ZESPÓŁ:")) {}

        if (!st.hasMoreTokens()) {
            doc.add(new Paragraph("Brak danych o zadaniach zespołu.", normalFont));
            addReportFooter(doc);
            return;
        }

        st = new StringTokenizer(content, "\n");

        boolean foundTeam = false;
        boolean processingTasks = false;
        PdfPTable table = null;

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            if (line.startsWith("=== ZESPÓŁ:")) {
                foundTeam = true;

                if (table != null) doc.add(table);
                processingTasks = false;

                Paragraph h = new Paragraph(line.replace("===", "").trim(), subtitleFont);
                h.setSpacingBefore(10);
                h.setSpacingAfter(5);
                doc.add(h);
            } else if (foundTeam && line.startsWith("ZADANIA ZESPOŁU")) {
                processingTasks = true;

                Paragraph taskHeader = new Paragraph("Zadania zespołu:", sectionFont);
                taskHeader.setSpacingBefore(10);
                taskHeader.setSpacingAfter(5);
                doc.add(taskHeader);

                table = new PdfPTable(4);
                table.setWidthPercentage(100);
                try {
                    table.setWidths(new float[]{40, 20, 20, 20});
                } catch (DocumentException e) {
                }

                PdfPCell headerTitle = new PdfPCell(new Phrase("Tytuł zadania", tableHeaderFont));
                headerTitle.setBackgroundColor(primaryColor);
                headerTitle.setPadding(5);
                table.addCell(headerTitle);

                PdfPCell headerStatus = new PdfPCell(new Phrase("Status", tableHeaderFont));
                headerStatus.setBackgroundColor(primaryColor);
                headerStatus.setPadding(5);
                table.addCell(headerStatus);

                PdfPCell headerPriority = new PdfPCell(new Phrase("Priorytet", tableHeaderFont));
                headerPriority.setBackgroundColor(primaryColor);
                headerPriority.setPadding(5);
                table.addCell(headerPriority);

                PdfPCell headerAssignee = new PdfPCell(new Phrase("Przypisane do", tableHeaderFont));
                headerAssignee.setBackgroundColor(primaryColor);
                headerAssignee.setPadding(5);
                table.addCell(headerAssignee);
            } else if (processingTasks && line.startsWith("- ")) {
                String taskLine = line.substring(2).trim();

                String title = taskLine;
                String status = "Nieznany";
                String priority = "Średni";
                String assignee = "Nieprzypisane";

                if (taskLine.contains("(")) {
                    int startIndex = taskLine.indexOf("(");
                    int endIndex = taskLine.lastIndexOf(")");
                    if (endIndex > startIndex) {
                        title = taskLine.substring(0, startIndex).trim();
                        String properties = taskLine.substring(startIndex + 1, endIndex);

                        if (properties.contains("Status:")) {
                            int statusIndex = properties.indexOf("Status:") + 7;
                            int statusEndIndex = properties.indexOf(",", statusIndex);
                            if (statusEndIndex == -1) statusEndIndex = properties.length();
                            status = properties.substring(statusIndex, statusEndIndex).trim();
                        }

                        if (properties.contains("Priorytet:")) {
                            int prioIndex = properties.indexOf("Priorytet:") + 10;
                            int prioEndIndex = properties.indexOf(",", prioIndex);
                            if (prioEndIndex == -1) prioEndIndex = properties.length();
                            priority = properties.substring(prioIndex, prioEndIndex).trim();
                        }
                    }
                }

                PdfPCell titleCell = new PdfPCell(new Phrase(title, tableDataFont));
                titleCell.setPadding(5);
                table.addCell(titleCell);

                PdfPCell statusCell = new PdfPCell(new Phrase(status, tableDataFont));
                statusCell.setPadding(5);
                table.addCell(statusCell);

                PdfPCell priorityCell = new PdfPCell(new Phrase(priority, tableDataFont));
                priorityCell.setPadding(5);
                table.addCell(priorityCell);

                PdfPCell assigneeCell = new PdfPCell(new Phrase(assignee, tableDataFont));
                assigneeCell.setPadding(5);
                table.addCell(assigneeCell);
            }
        }

        if (table != null) doc.add(table);
        if (!foundTeam) {
            doc.add(new Paragraph("Brak danych o zadaniach zespołu.", normalFont));
        }

        addReportFooter(doc);
    }

    private void addReportFooter(Document doc) throws DocumentException {
        addSeparator(doc);
        Paragraph f = new Paragraph(
                "Ten raport został wygenerowany automatycznie przez system TaskApp.", smallFont
        );
        f.setAlignment(Element.ALIGN_CENTER);
        f.setSpacingAfter(5);
        doc.add(f);
    }
}