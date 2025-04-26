package pl.rozowi.app.services;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import pl.rozowi.app.models.User;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

public class ReportService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font SECTION_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

    public void generateTeamsStructurePdf(String filename, String reportContent) throws IOException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            writer.setPageEvent(new FooterPageEvent());
            document.open();

            // Add report header with nice formatting
            addReportHeader(document, "Raport: Struktura Zespołów");

            // Parse and format the report content
            parseAndFormatTeamsReport(document, reportContent);

        } catch (DocumentException e) {
            throw new IOException("Error creating PDF: " + e.getMessage());
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    public void generateUsersReportPdf(String filename, String reportContent) throws IOException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            writer.setPageEvent(new FooterPageEvent());
            document.open();

            // Add report header with nice formatting
            addReportHeader(document, "Raport: Użytkownicy Systemu");

            // Parse and format the report content
            parseAndFormatUsersReport(document, reportContent);

        } catch (DocumentException e) {
            throw new IOException("Error creating PDF: " + e.getMessage());
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    public void generateProjectsOverviewPdf(String filename, String reportContent) throws IOException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            writer.setPageEvent(new FooterPageEvent());
            document.open();

            // Add report header with nice formatting
            addReportHeader(document, "Raport: Przegląd Projektów");

            // Parse and format the report content
            parseAndFormatProjectsReport(document, reportContent);

        } catch (DocumentException e) {
            throw new IOException("Error creating PDF: " + e.getMessage());
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private void addReportHeader(Document document, String title) throws DocumentException {
        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        document.add(titlePara);

        String dateStr = "Wygenerowano: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Paragraph datePara = new Paragraph(dateStr, NORMAL_FONT);
        datePara.setAlignment(Element.ALIGN_CENTER);
        document.add(datePara);

        document.add(Chunk.NEWLINE);
    }

    // Inner class for footer page event
    private static class FooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase("Strona " + writer.getPageNumber(), SMALL_FONT);
            float x = (document.right() + document.left()) / 2;
            float y = document.bottom() - 10;
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer, x, y, 0);
        }
    }

    // --- Parsing and formatting methods unchanged ---

    private void parseAndFormatTeamsReport(Document document, String reportContent) throws DocumentException {
        StringTokenizer lines = new StringTokenizer(reportContent, "\n");
        if (lines.hasMoreTokens()) lines.nextToken();
        if (lines.hasMoreTokens()) lines.nextToken();
        if (lines.hasMoreTokens()) lines.nextToken();

        PdfPTable table = null;
        boolean inTeamSection = false;
        boolean inMembersSection = false;
        boolean inTasksSection = false;

        while (lines.hasMoreTokens()) {
            String line = lines.nextToken();
            if (line.startsWith("=== ZESPÓŁ: ")) {
                if (table != null) document.add(table);
                Paragraph teamHeader = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
                teamHeader.setSpacingBefore(10);
                document.add(teamHeader);
                table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{30, 70});
                inTeamSection = true;
                inMembersSection = false;
                inTasksSection = false;
            } else if (line.startsWith("CZŁONKOWIE ZESPOŁU:")) {
                document.add(new Paragraph("CZŁONKOWIE ZESPOŁU:", SECTION_FONT));
                inTeamSection = false;
                inMembersSection = true;
                inTasksSection = false;
            } else if (line.startsWith("ZADANIA ZESPOŁU")) {
                document.add(new Paragraph(line, SECTION_FONT));
                inTeamSection = false;
                inMembersSection = false;
                inTasksSection = true;
            } else if (line.trim().isEmpty()) {
                if (table != null) document.add(table);
                table = null;
                inTeamSection = false;
                inMembersSection = false;
                inTasksSection = false;
            } else {
                if (inTeamSection && table != null && line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    PdfPCell keyCell = new PdfPCell(new Phrase(parts[0].trim() + ":", NORMAL_FONT));
                    keyCell.setBorder(PdfPCell.NO_BORDER);
                    table.addCell(keyCell);
                    PdfPCell valueCell = new PdfPCell(new Phrase(parts[1].trim(), NORMAL_FONT));
                    valueCell.setBorder(PdfPCell.NO_BORDER);
                    table.addCell(valueCell);
                } else if (inMembersSection && line.startsWith("-")) {
                    document.add(new Paragraph("  " + line, NORMAL_FONT));
                } else if (inTasksSection && line.startsWith("-")) {
                    document.add(new Paragraph("  " + line, NORMAL_FONT));
                } else {
                    document.add(new Paragraph(line, NORMAL_FONT));
                }
            }
        }
        if (table != null) document.add(table);
    }

    private void parseAndFormatUsersReport(Document document, String reportContent) throws DocumentException {
        StringTokenizer lines = new StringTokenizer(reportContent, "\n");
        if (lines.hasMoreTokens()) lines.nextToken();
        if (lines.hasMoreTokens()) lines.nextToken();
        if (lines.hasMoreTokens()) lines.nextToken();

        if (lines.hasMoreTokens()) {
            document.add(new Paragraph(lines.nextToken(), NORMAL_FONT));
        }

        PdfPTable usersTable = new PdfPTable(3);
        usersTable.setWidthPercentage(100);
        usersTable.setWidths(new float[]{50, 25, 25});

        PdfPCell nameHeader = new PdfPCell(new Phrase("Użytkownik", SECTION_FONT));
        nameHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        nameHeader.setPadding(5);
        usersTable.addCell(nameHeader);

        PdfPCell roleHeader = new PdfPCell(new Phrase("Rola", SECTION_FONT));
        roleHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        roleHeader.setPadding(5);
        usersTable.addCell(roleHeader);

        PdfPCell teamHeader = new PdfPCell(new Phrase("Zespół", SECTION_FONT));
        teamHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        teamHeader.setPadding(5);
        usersTable.addCell(teamHeader);

        String currentRole = "";

        while (lines.hasMoreTokens()) {
            String line = lines.nextToken();
            if (line.trim().isEmpty()) continue;
            if (line.startsWith("=== ROLA: ")) {
                currentRole = line.replace("=== ROLA: ", "").replace(" ===", "").trim();
            } else if (line.startsWith("-")) {
                String userName = line.substring(1).trim();
                int emailStart = userName.lastIndexOf("(");
                int emailEnd = userName.lastIndexOf(")");
                String userDisplayName = userName.substring(0, emailStart).trim();
                PdfPCell nameCell = new PdfPCell(new Phrase(userDisplayName, NORMAL_FONT));
                nameCell.setPadding(5);
                usersTable.addCell(nameCell);

                PdfPCell roleCell = new PdfPCell(new Phrase(currentRole, NORMAL_FONT));
                roleCell.setPadding(5);
                usersTable.addCell(roleCell);

                if (lines.hasMoreTokens()) {
                    String teamLine = lines.nextToken();
                    if (teamLine.trim().startsWith("Zespół:")) {
                        String teamName = teamLine.replace("Zespół:", "").trim();
                        PdfPCell teamCell = new PdfPCell(new Phrase(teamName, NORMAL_FONT));
                        teamCell.setPadding(5);
                        usersTable.addCell(teamCell);
                    } else {
                        usersTable.addCell(new PdfPCell(new Phrase("", NORMAL_FONT)));
                    }
                } else {
                    usersTable.addCell(new PdfPCell(new Phrase("", NORMAL_FONT)));
                }
            }
        }

        document.add(usersTable);
    }

    private void parseAndFormatProjectsReport(Document document, String reportContent) throws DocumentException {
        StringTokenizer lines = new StringTokenizer(reportContent, "\n");
        if (lines.hasMoreTokens()) lines.nextToken();
        if (lines.hasMoreTokens()) lines.nextToken();
        if (lines.hasMoreTokens()) lines.nextToken();

        if (lines.hasMoreTokens()) {
            document.add(new Paragraph(lines.nextToken(), NORMAL_FONT));
        }
        if (lines.hasMoreTokens()) lines.nextToken();

        PdfPTable table = null;
        boolean inProjectSection = false;

        while (lines.hasMoreTokens()) {
            String line = lines.nextToken();
            if (line.startsWith("=== PROJEKT: ")) {
                if (table != null) document.add(table);
                Paragraph projectHeader = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
                projectHeader.setSpacingBefore(10);
                document.add(projectHeader);
                table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{30, 70});
                inProjectSection = true;
            } else if (line.trim().isEmpty()) {
                if (table != null) document.add(table);
                table = null;
                inProjectSection = false;
            } else if (inProjectSection && table != null) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    PdfPCell keyCell = new PdfPCell(new Phrase(parts[0].trim() + ":", NORMAL_FONT));
                    keyCell.setBorder(PdfPCell.NO_BORDER);
                    table.addCell(keyCell);
                    PdfPCell valueCell = new PdfPCell(new Phrase(parts[1].trim(), NORMAL_FONT));
                    valueCell.setBorder(PdfPCell.NO_BORDER);
                    table.addCell(valueCell);
                } else if (line.startsWith("-")) {
                    PdfPCell listItemCell = new PdfPCell(new Phrase("    " + line, NORMAL_FONT));
                    listItemCell.setBorder(PdfPCell.NO_BORDER);
                    listItemCell.setColspan(2);
                    table.addCell(listItemCell);
                }
            } else {
                document.add(new Paragraph(line, NORMAL_FONT));
            }
        }

        if (table != null) document.add(table);
    }
}
