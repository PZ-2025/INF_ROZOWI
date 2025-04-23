package pl.rozowi.app.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportService {

    private final UserDAO userDAO;
    private final TaskDAO taskDAO;
    private final ProjectDAO projectDAO;
    private final TeamDAO teamDAO;
    private final TaskAssignmentDAO taskAssignmentDAO;

    private static final String REPORTS_DIR = "reports";
    private static final BaseColor HEADER_COLOR = new BaseColor(66, 133, 244);
    private static final BaseColor SUBHEADER_COLOR = new BaseColor(100, 160, 255);

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font SUBHEADER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);

    public ReportService(UserDAO userDAO, TaskDAO taskDAO, ProjectDAO projectDAO, TeamDAO teamDAO, TaskAssignmentDAO taskAssignmentDAO) {
        this.userDAO = userDAO;
        this.taskDAO = taskDAO;
        this.projectDAO = projectDAO;
        this.teamDAO = teamDAO;
        this.taskAssignmentDAO = taskAssignmentDAO;

        // Upewnij się, że katalog na raporty istnieje
        File reportsDir = new File(REPORTS_DIR);
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
    }

    public String generateEmployeeWorkloadReport(Integer managerId, LocalDate startDate, LocalDate endDate, String statusFilter, String priorityFilter) {
        try {
            // Nazwa pliku raportu
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = REPORTS_DIR + "/workload_report_" + dateFormat.format(new Date()) + ".pdf";

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Tytuł raportu
            Paragraph title = new Paragraph("Raport obciążenia pracowników", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Informacje o filtrach
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            Paragraph filters = new Paragraph();
            filters.add(new Chunk("Zakres dat: ", BOLD_FONT));
            filters.add(new Chunk(startDate.format(formatter) + " do " + endDate.format(formatter), NORMAL_FONT));
            filters.add(Chunk.NEWLINE);

            if (statusFilter != null && !statusFilter.isEmpty()) {
                filters.add(new Chunk("Status: ", BOLD_FONT));
                filters.add(new Chunk(statusFilter, NORMAL_FONT));
                filters.add(Chunk.NEWLINE);
            }

            if (priorityFilter != null && !priorityFilter.isEmpty()) {
                filters.add(new Chunk("Priorytet: ", BOLD_FONT));
                filters.add(new Chunk(priorityFilter, NORMAL_FONT));
                filters.add(Chunk.NEWLINE);
            }

            filters.setSpacingAfter(15);
            document.add(filters);

            // Pobierz dane pracowników i ich zadania
            java.util.List<User> employees = new java.util.ArrayList<>();

            if (managerId != null) {
                // Jeśli to kierownik, pobierz tylko jego pracowników
                java.util.List<Team> managerTeams = teamDAO.getTeamsForUser(managerId);

                for (Team team : managerTeams) {
                    java.util.List<User> teamMembers = teamDAO.getTeamMembers(team.getId());
                    for (User member : teamMembers) {
                        employees.add(member);
                    }
                }
            } else {
                // Dla admina pobierz wszystkich użytkowników
                employees = userDAO.getAllUsers();
            }

            // Tabela z danymi pracowników i ich zadań
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidths = {2f, 1f, 1f, 1f, 1f};
            table.setWidths(columnWidths);

            // Nagłówki tabeli
            addTableHeader(table, new String[]{"Pracownik", "Liczba zadań", "Nowe", "W toku", "Zakończone"});

            // Dane pracowników
            for (User employee : employees) {
                java.util.List<Task> tasks = taskDAO.getTasksForUser(employee.getId());

                // Filtruj zadania według warunków
                java.util.List<Task> filteredTasks = filterTasks(tasks, startDate, endDate, statusFilter, priorityFilter);

                // Licz statystyki zadań
                int totalTasks = filteredTasks.size();
                int newTasks = countTasksByStatus(filteredTasks, "Nowe");
                int inProgressTasks = countTasksByStatus(filteredTasks, "W toku");
                int completedTasks = countTasksByStatus(filteredTasks, "Zakończone");

                // Dodaj wiersz z danymi pracownika
                PdfPCell nameCell = new PdfPCell(new Phrase(employee.getName() + " " + employee.getLastName(), NORMAL_FONT));
                PdfPCell totalCell = new PdfPCell(new Phrase(String.valueOf(totalTasks), NORMAL_FONT));
                PdfPCell newCell = new PdfPCell(new Phrase(String.valueOf(newTasks), NORMAL_FONT));
                PdfPCell inProgressCell = new PdfPCell(new Phrase(String.valueOf(inProgressTasks), NORMAL_FONT));
                PdfPCell completedCell = new PdfPCell(new Phrase(String.valueOf(completedTasks), NORMAL_FONT));

                nameCell.setPadding(5);
                totalCell.setPadding(5);
                newCell.setPadding(5);
                inProgressCell.setPadding(5);
                completedCell.setPadding(5);

                nameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                newCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                inProgressCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                completedCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                table.addCell(nameCell);
                table.addCell(totalCell);
                table.addCell(newCell);
                table.addCell(inProgressCell);
                table.addCell(completedCell);
            }

            document.add(table);

            // Stopka dokumentu
            Paragraph footer = new Paragraph("Raport wygenerowany: " + new Date(), NORMAL_FONT);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateProjectProgressReport(Integer projectId, LocalDate startDate, LocalDate endDate, String statusFilter) {
        try {
            // Nazwa pliku raportu
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = REPORTS_DIR + "/project_progress_" + dateFormat.format(new Date()) + ".pdf";

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Tytuł raportu
            Paragraph title = new Paragraph("Raport postępu projektu", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Informacje o projekcie
            Project project = null;
            java.util.List<Project> projects = new java.util.ArrayList<>();

            if (projectId != null) {
                // Jeśli ID projektu jest określone, znajdź tylko ten projekt
                java.util.List<Project> allProjects = projectDAO.getAllProjects();
                for (Project p : allProjects) {
                    if (p.getId() == projectId) {
                        project = p;
                        projects.add(p);
                        break;
                    }
                }
            } else {
                // W przeciwnym razie pobierz wszystkie projekty
                projects = projectDAO.getAllProjects();
                if (!projects.isEmpty()) {
                    project = projects.get(0);
                }
            }

            if (project != null) {
                Paragraph projectInfo = new Paragraph();
                projectInfo.add(new Chunk("Projekt: ", BOLD_FONT));
                projectInfo.add(new Chunk(project.getName(), NORMAL_FONT));
                projectInfo.add(Chunk.NEWLINE);

                if (project.getDescription() != null && !project.getDescription().isEmpty()) {
                    projectInfo.add(new Chunk("Opis: ", BOLD_FONT));
                    projectInfo.add(new Chunk(project.getDescription(), NORMAL_FONT));
                    projectInfo.add(Chunk.NEWLINE);
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                projectInfo.add(new Chunk("Data rozpoczęcia: ", BOLD_FONT));
                projectInfo.add(new Chunk(project.getStartDate().format(formatter), NORMAL_FONT));
                projectInfo.add(Chunk.NEWLINE);

                projectInfo.add(new Chunk("Data zakończenia: ", BOLD_FONT));
                projectInfo.add(new Chunk(project.getEndDate().format(formatter), NORMAL_FONT));
                projectInfo.add(Chunk.NEWLINE);

                projectInfo.setSpacingAfter(15);
                document.add(projectInfo);

                // Dla każdego projektu dodaj tabelę z zadaniami
                for (Project p : projects) {
                    if (projects.size() > 1) {
                        // Jeśli jest więcej niż jeden projekt, dodaj nagłówek z nazwą projektu
                        Paragraph projectHeader = new Paragraph(p.getName(), BOLD_FONT);
                        projectHeader.setSpacingBefore(10);
                        projectHeader.setSpacingAfter(5);
                        document.add(projectHeader);
                    }

                    // Pobierz zadania dla projektu
                    java.util.List<Task> tasks = taskDAO.getTasksByProjectId(p.getId());

                    // Filtruj zadania według warunków
                    java.util.List<Task> filteredTasks = filterTasks(tasks, startDate, endDate, statusFilter, null);

                    if (filteredTasks.isEmpty()) {
                        Paragraph noTasks = new Paragraph("Brak zadań spełniających kryteria filtrowania.", NORMAL_FONT);
                        document.add(noTasks);
                        continue;
                    }

                    // Tabela z zadaniami
                    PdfPTable taskTable = new PdfPTable(5);
                    taskTable.setWidthPercentage(100);
                    taskTable.setSpacingBefore(5f);
                    taskTable.setSpacingAfter(10f);

                    float[] columnWidths = {2.5f, 1f, 1f, 1f, 1.5f};
                    taskTable.setWidths(columnWidths);

                    // Nagłówki tabeli
                    addTableHeader(taskTable, new String[]{"Zadanie", "Status", "Priorytet", "Termin", "Przypisane do"});

                    // Dane zadań
                    for (Task task : filteredTasks) {
                        PdfPCell titleCell = new PdfPCell(new Phrase(task.getTitle(), NORMAL_FONT));
                        PdfPCell statusCell = new PdfPCell(new Phrase(task.getStatus(), NORMAL_FONT));
                        PdfPCell priorityCell = new PdfPCell(new Phrase(task.getPriority(), NORMAL_FONT));
                        PdfPCell deadlineCell = new PdfPCell(new Phrase(task.getEndDate(), NORMAL_FONT));

                        // Pobierz informacje o przypisanym użytkowniku
                        String assignedTo = task.getAssignedEmail() != null ? task.getAssignedEmail() : "-";
                        PdfPCell assignedCell = new PdfPCell(new Phrase(assignedTo, NORMAL_FONT));

                        titleCell.setPadding(5);
                        statusCell.setPadding(5);
                        priorityCell.setPadding(5);
                        deadlineCell.setPadding(5);
                        assignedCell.setPadding(5);

                        taskTable.addCell(titleCell);
                        taskTable.addCell(statusCell);
                        taskTable.addCell(priorityCell);
                        taskTable.addCell(deadlineCell);
                        taskTable.addCell(assignedCell);
                    }

                    document.add(taskTable);

                    // Podsumowanie postępu projektu
                    Paragraph summary = new Paragraph();
                    int totalTasks = filteredTasks.size();
                    int completedTasks = countTasksByStatus(filteredTasks, "Zakończone");
                    double progressPercentage = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

                    summary.add(new Chunk("Postęp projektu: ", BOLD_FONT));
                    summary.add(new Chunk(String.format("%.1f%%", progressPercentage), NORMAL_FONT));
                    summary.add(Chunk.NEWLINE);
                    summary.add(new Chunk("Zadania zakończone: ", BOLD_FONT));
                    summary.add(new Chunk(completedTasks + "/" + totalTasks, NORMAL_FONT));

                    document.add(summary);
                }
            } else {
                document.add(new Paragraph("Nie znaleziono projektów.", NORMAL_FONT));
            }

            // Stopka dokumentu
            Paragraph footer = new Paragraph("Raport wygenerowany: " + new Date(), NORMAL_FONT);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateUserPerformanceReport(Integer userId, LocalDate startDate, LocalDate endDate) {
        try {
            // Nazwa pliku raportu
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = REPORTS_DIR + "/user_performance_" + dateFormat.format(new Date()) + ".pdf";

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Tytuł raportu
            Paragraph title = new Paragraph("Raport wydajności użytkowników", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Informacje o filtrach
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            Paragraph filters = new Paragraph();
            filters.add(new Chunk("Zakres dat: ", BOLD_FONT));
            filters.add(new Chunk(startDate.format(formatter) + " do " + endDate.format(formatter), NORMAL_FONT));
            filters.add(Chunk.NEWLINE);

            filters.setSpacingAfter(15);
            document.add(filters);

            // Pobierz dane użytkowników
            java.util.List<User> users = new java.util.ArrayList<>();
            if (userId != null) {
                // Jeśli ID użytkownika jest określone, znajdź tylko tego użytkownika
                java.util.List<User> allUsers = userDAO.getAllUsers();
                for (User u : allUsers) {
                    if (u.getId() == userId) {
                        users.add(u);
                        break;
                    }
                }
            } else {
                // W przeciwnym razie pobierz wszystkich użytkowników
                users = userDAO.getAllUsers();
            }

            // Tabela z danymi wydajności
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidths = {2f, 1f, 1f, 1f, 1f};
            table.setWidths(columnWidths);

            // Nagłówki tabeli
            addTableHeader(table, new String[]{"Użytkownik", "Ukończone zadania", "Opóźnione", "W toku", "Wydajność %"});

            // Dane użytkowników
            for (User user : users) {
                java.util.List<Task> tasks = taskDAO.getTasksForUser(user.getId());

                // Filtruj zadania według dat
                java.util.List<Task> filteredTasks = filterTasks(tasks, startDate, endDate, null, null);

                // Oblicz statystyki
                int completedTasks = countTasksByStatus(filteredTasks, "Zakończone");
                int inProgressTasks = countTasksByStatus(filteredTasks, "W toku");
                int delayedTasks = countDelayedTasks(filteredTasks);

                // Oblicz wydajność (ukończone zadania / wszystkie zadania) * 100%
                double efficiency = filteredTasks.size() > 0 ? (double) completedTasks / filteredTasks.size() * 100 : 0;

                // Dodaj wiersz z danymi
                PdfPCell nameCell = new PdfPCell(new Phrase(user.getName() + " " + user.getLastName(), NORMAL_FONT));
                PdfPCell completedCell = new PdfPCell(new Phrase(String.valueOf(completedTasks), NORMAL_FONT));
                PdfPCell delayedCell = new PdfPCell(new Phrase(String.valueOf(delayedTasks), NORMAL_FONT));
                PdfPCell inProgressCell = new PdfPCell(new Phrase(String.valueOf(inProgressTasks), NORMAL_FONT));
                PdfPCell efficiencyCell = new PdfPCell(new Phrase(String.format("%.1f%%", efficiency), NORMAL_FONT));

                nameCell.setPadding(5);
                completedCell.setPadding(5);
                delayedCell.setPadding(5);
                inProgressCell.setPadding(5);
                efficiencyCell.setPadding(5);

                nameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                completedCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                delayedCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                inProgressCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                efficiencyCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                table.addCell(nameCell);
                table.addCell(completedCell);
                table.addCell(delayedCell);
                table.addCell(inProgressCell);
                table.addCell(efficiencyCell);
            }

            document.add(table);

            // Stopka dokumentu
            Paragraph footer = new Paragraph("Raport wygenerowany: " + new Date(), NORMAL_FONT);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metody pomocnicze

    private java.util.List<Task> filterTasks(java.util.List<Task> tasks, LocalDate startDate, LocalDate endDate, String statusFilter, String priorityFilter) {
        java.util.List<Task> filteredTasks = new java.util.ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Task task : tasks) {
            // Sprawdź warunki filtrów
            boolean matchesStatus = statusFilter == null || statusFilter.isEmpty() ||
                                    (task.getStatus() != null && task.getStatus().equals(statusFilter));

            boolean matchesPriority = priorityFilter == null || priorityFilter.isEmpty() ||
                                      (task.getPriority() != null && task.getPriority().equals(priorityFilter));

            // Sprawdź zakres dat
            boolean inDateRange = true;
            if (task.getStartDate() != null && !task.getStartDate().isEmpty()) {
                try {
                    LocalDate taskStartDate = LocalDate.parse(task.getStartDate(), formatter);
                    inDateRange = !taskStartDate.isBefore(startDate);
                } catch (Exception e) {
                    // W przypadku błędu parsowania daty, ignoruj to zadanie
                    inDateRange = false;
                }
            }

            if (task.getEndDate() != null && !task.getEndDate().isEmpty() && inDateRange) {
                try {
                    LocalDate taskEndDate = LocalDate.parse(task.getEndDate(), formatter);
                    inDateRange = !taskEndDate.isAfter(endDate);
                } catch (Exception e) {
                    // W przypadku błędu parsowania daty, ignoruj to zadanie
                    inDateRange = false;
                }
            }

            if (matchesStatus && matchesPriority && inDateRange) {
                filteredTasks.add(task);
            }
        }

        return filteredTasks;
    }

    private int countTasksByStatus(java.util.List<Task> tasks, String status) {
        int count = 0;
        for (Task task : tasks) {
            if (task.getStatus() != null && task.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    private int countDelayedTasks(java.util.List<Task> tasks) {
        int count = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        for (Task task : tasks) {
            if (task.getEndDate() != null && !task.getEndDate().isEmpty() &&
                !task.getStatus().equals("Zakończone")) {
                try {
                    LocalDate endDate = LocalDate.parse(task.getEndDate(), formatter);
                    if (endDate.isBefore(today)) {
                        count++;
                    }
                } catch (Exception e) {
                    // Ignoruj błędy parsowania daty
                }
            }
        }

        return count;
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(HEADER_COLOR);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    // Tworzy nowy rekord raportu w bazie danych
    public boolean saveReportRecord(String reportName, String reportType, String reportScope, int createdBy, String exportedFile) {
        Report report = new Report();
        report.setReportName(reportName);
        report.setReportType(reportType);
        report.setReportScope(reportScope);
        report.setCreatedBy(createdBy);
        report.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        report.setExportedFile(exportedFile);

        ReportDAO reportDAO = new ReportDAO();
        return reportDAO.insertReport(report);
    }
}