//package pl.rozowi.app.services;
//
//import com.itextpdf.text.*;
//import com.itextpdf.text.pdf.BaseFont;
//import com.itextpdf.text.pdf.ColumnText;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfPageEventHelper;
//import com.itextpdf.text.pdf.PdfPTable;
//import com.itextpdf.text.pdf.PdfPCell;
//import com.itextpdf.text.pdf.PdfWriter;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.StringTokenizer;
//
//public class ReportService {
//
//    private Font TITLE_FONT;
//    private Font SUBTITLE_FONT;
//    private Font SECTION_FONT;
//    private Font NORMAL_FONT;
//    private Font SMALL_FONT;
//    private Font TABLE_HEADER_FONT;
//    private Font TABLE_DATA_FONT;
//
//    private BaseColor PRIMARY_COLOR = new BaseColor(0, 123, 255);
//    private BaseColor SECONDARY_COLOR = new BaseColor(30, 30, 47);
//    private BaseColor LIGHT_COLOR = new BaseColor(240, 240, 245);
//
//    public ReportService() {
//        try {
//            BaseFont base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
//            TITLE_FONT = new Font(base, 18, Font.BOLD, PRIMARY_COLOR);
//            SUBTITLE_FONT = new Font(base, 14, Font.BOLD, PRIMARY_COLOR);
//            SECTION_FONT = new Font(base, 12, Font.BOLD);
//            NORMAL_FONT = new Font(base, 10, Font.NORMAL);
//            SMALL_FONT = new Font(base, 8, Font.NORMAL);
//            TABLE_HEADER_FONT = new Font(base, 10, Font.BOLD, BaseColor.WHITE);
//            TABLE_DATA_FONT = new Font(base, 9, Font.NORMAL);
//        } catch (Exception e) {
//            TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, PRIMARY_COLOR);
//            SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
//            SECTION_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
//            NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
//            SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
//            TABLE_HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
//            TABLE_DATA_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
//        }
//    }
//
//    public void generateTeamsStructurePdf(String filename, String content) throws IOException {
//        Document doc = new Document(PageSize.A4);
//        try {
//            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
//            writer.setPageEvent(new FooterEvent());
//            doc.open();
//            addReportHeader(doc, "Raport: Struktura Zespołów");
//            addSeparator(doc);
//            parseTeams(doc, content);
//        } catch (DocumentException de) {
//            throw new IOException(de.getMessage(), de);
//        } finally {
//            if (doc.isOpen()) doc.close();
//        }
//    }
//
//    public void generateUsersReportPdf(String filename, String content) throws IOException {
//        Document doc = new Document(PageSize.A4);
//        try {
//            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
//            writer.setPageEvent(new FooterEvent());
//            doc.open();
//            addReportHeader(doc, "Raport: Użytkownicy Systemu");
//            addSeparator(doc);
//            parseUsers(doc, content);
//        } catch (DocumentException de) {
//            throw new IOException(de.getMessage(), de);
//        } finally {
//            if (doc.isOpen()) doc.close();
//        }
//    }
//
//    public void generateProjectsOverviewPdf(String filename, String content) throws IOException {
//        Document doc = new Document(PageSize.A4);
//        try {
//            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
//            writer.setPageEvent(new FooterEvent());
//            doc.open();
//            addReportHeader(doc, "Raport: Przegląd Projektów");
//            addSeparator(doc);
//            parseProjects(doc, content);
//        } catch (DocumentException de) {
//            throw new IOException(de.getMessage(), de);
//        } finally {
//            if (doc.isOpen()) doc.close();
//        }
//    }
//
//    private void addReportHeader(Document doc, String title) throws DocumentException {
//        Paragraph p = new Paragraph(title, TITLE_FONT);
//        p.setAlignment(Element.ALIGN_CENTER);
//        p.setSpacingAfter(10);
//        doc.add(p);
//        String date = "Wygenerowano: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        Paragraph d = new Paragraph(date, NORMAL_FONT);
//        d.setAlignment(Element.ALIGN_CENTER);
//        d.setSpacingAfter(15);
//        doc.add(d);
//    }
//
//    private void addSeparator(Document doc) throws DocumentException {
//        Chunk line = new Chunk(new com.itextpdf.text.pdf.draw.LineSeparator(0.5f, 100, PRIMARY_COLOR, Element.ALIGN_CENTER, -2));
//        Paragraph para = new Paragraph();
//        para.add(line);
//        doc.add(para);
//    }
//
//    private class FooterEvent extends PdfPageEventHelper {
//        @Override
//        public void onEndPage(PdfWriter writer, Document doc) {
//            PdfContentByte cb = writer.getDirectContent();
//            cb.setLineWidth(0.5f);
//            cb.setColorStroke(PRIMARY_COLOR);
//            float y = doc.bottom() - 5;
//            cb.moveTo(doc.left(), y);
//            cb.lineTo(doc.right(), y);
//            cb.stroke();
//            Phrase f = new Phrase("TaskApp - Strona " + writer.getPageNumber(), SMALL_FONT);
//            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, f, (doc.left() + doc.right())/2, doc.bottom()-20, 0);
//        }
//    }
//
//    // parse teams
//    private void parseTeams(Document doc, String content) throws DocumentException {
//        StringTokenizer st = new StringTokenizer(content, "\n");
//        // skip initial header lines until first team
//        PdfPTable table = null;
//        while (st.hasMoreTokens()) {
//            String tmp = st.nextToken();
//            if (tmp.startsWith("=== ZESPÓŁ:")) {
//                // process first header
//                Paragraph h = new Paragraph(tmp.replace("===", "").trim(), SUBTITLE_FONT);
//                h.setSpacingBefore(10);
//                h.setSpacingAfter(5);
//                doc.add(h);
//                table = new PdfPTable(2);
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{30, 70});
//                break;
//            }
//        }
//        // main loop for remaining lines
//        while (st.hasMoreTokens()) {
//            String line = st.nextToken();
//            if (line.startsWith("=== ZESPÓŁ:")) {
//                if (table != null) doc.add(table);
//                Paragraph h = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
//                h.setSpacingBefore(10);
//                h.setSpacingAfter(5);
//                doc.add(h);
//                table = new PdfPTable(2);
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{30, 70});
//            } else if (line.trim().isEmpty()) {
//                if (table != null) doc.add(table);
//                table = null;
//            } else if (table != null && line.contains(":")) {
//                String[] p = line.split(":", 2);
//                PdfPCell k = new PdfPCell(new Phrase(p[0].trim() + ":", NORMAL_FONT));
//                k.setBorder(Rectangle.NO_BORDER);
//                PdfPCell v = new PdfPCell(new Phrase(p[1].trim(), NORMAL_FONT));
//                v.setBorder(Rectangle.NO_BORDER);
//                table.addCell(k);
//                table.addCell(v);
//            } else {
//                doc.add(new Paragraph(line, NORMAL_FONT));
//            }
//        }
//        if (table != null) doc.add(table);
//        addReportFooter(doc);
//    }
//
//    // parse users
//    private void parseUsers(Document doc, String content) throws DocumentException {
//        StringTokenizer st = new StringTokenizer(content, "\n");
//        // skip initial header lines
//        for (int i = 0; i < 3 && st.hasMoreTokens(); i++) st.nextToken();
//        PdfPTable table = null;
//        String role = "";
//        while (st.hasMoreTokens()) {
//            String line = st.nextToken();
//            if (line.startsWith("=== ROLA:")) {
//                if (table != null) doc.add(table);
//                role = line.replace("=== ROLA:", "").replace("===", "").trim();
//                Paragraph h = new Paragraph(role, SUBTITLE_FONT);
//                h.setSpacingBefore(10);
//                h.setSpacingAfter(5);
//                doc.add(h);
//                table = new PdfPTable(3);
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{40, 30, 30});
//                String[] hdr = {"Użytkownik", "Email", "Zespół"};
//                for (String t : hdr) {
//                    PdfPCell c = new PdfPCell(new Phrase(t, TABLE_HEADER_FONT));
//                    c.setBackgroundColor(PRIMARY_COLOR);
//                    c.setPadding(5);
//                    table.addCell(c);
//                }
//            } else if (line.startsWith("-")) {
//                String u = line.substring(1).trim();
//                String name = u, email = "";
//                int s = u.lastIndexOf("(");
//                int e = u.lastIndexOf(")");
//                if (s >= 0 && e > s) {
//                    name = u.substring(0, s).trim();
//                    email = u.substring(s + 1, e);
//                }
//                String team = "Brak";
//                if (st.hasMoreTokens()) {
//                    String l2 = st.nextToken();
//                    if (l2.trim().startsWith("Zespół:")) {
//                        team = l2.trim().replace("Zespół:", "").trim();
//                    }
//                }
//                if (table != null) {
//                    PdfPCell n = new PdfPCell(new Phrase(name, TABLE_DATA_FONT));
//                    n.setPadding(5);
//                    table.addCell(n);
//                    PdfPCell em = new PdfPCell(new Phrase(email, TABLE_DATA_FONT));
//                    em.setPadding(5);
//                    table.addCell(em);
//                    PdfPCell tm = new PdfPCell(new Phrase(team, TABLE_DATA_FONT));
//                    tm.setPadding(5);
//                    table.addCell(tm);
//                }
//            }
//        }
//        if (table != null) doc.add(table);
//        addReportFooter(doc);
//    }
//
//    // parse projects (unchanged)
//    private void parseProjects(Document doc, String content) throws DocumentException {
//        StringTokenizer st = new StringTokenizer(content, "\n");
//        for (int i = 0; i < 3 && st.hasMoreTokens(); i++) st.nextToken();
//        PdfPTable table = null;
//        while (st.hasMoreTokens()) {
//            String line = st.nextToken();
//            if (line.startsWith("=== PROJEKT: ")) {
//                if (table != null) doc.add(table);
//                Paragraph h = new Paragraph(line.replace("===", "").trim(), SUBTITLE_FONT);
//                h.setSpacingBefore(10);
//                h.setSpacingAfter(5);
//                doc.add(h);
//                table = new PdfPTable(2);
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{30, 70});
//            } else if (line.trim().isEmpty()) {
//                if (table != null) doc.add(table);
//                table = null;
//            } else if (table != null && line.contains(":")) {
//                String[] p = line.split(":", 2);
//                PdfPCell k = new PdfPCell(new Phrase(p[0].trim() + ":", NORMAL_FONT));
//                k.setBorder(Rectangle.NO_BORDER);
//                PdfPCell v = new PdfPCell(new Phrase(p[1].trim(), NORMAL_FONT));
//                v.setBorder(Rectangle.NO_BORDER);
//                table.addCell(k);
//                table.addCell(v);
//            } else if (line.startsWith("Zespoły:")) {
//                if (table != null) doc.add(table);
//                table = null;
//                Paragraph mh = new Paragraph("Zespoły projektu:", SECTION_FONT);
//                mh.setSpacingBefore(10);
//                mh.setSpacingAfter(5);
//                doc.add(mh);
//            } else if (line.startsWith("-")) {
//                Paragraph pl = new Paragraph(line.substring(1).trim(), NORMAL_FONT);
//                pl.setIndentationLeft(10);
//                doc.add(pl);
//            }
//        }
//        if (table != null) doc.add(table);
//        addReportFooter(doc);
//    }
//
//    private void addReportFooter(Document doc) throws DocumentException {
//        addSeparator(doc);
//        Paragraph f = new Paragraph(
//                "Ten raport został wygenerowany automatycznie przez system TaskApp.", SMALL_FONT
//        );
//        f.setAlignment(Element.ALIGN_CENTER);
//        f.setSpacingAfter(5);
//        doc.add(f);
//    }
//}