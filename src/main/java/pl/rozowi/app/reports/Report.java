package pl.rozowi.app.reports;

import java.sql.Timestamp;

public class Report {
    private int id;
    private String reportName;
    private String reportType;
    private String reportScope;
    private int createdBy;
    private Timestamp createdAt;
    private String exportedFile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportScope() {
        return reportScope;
    }

    public void setReportScope(String reportScope) {
        this.reportScope = reportScope;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getExportedFile() {
        return exportedFile;
    }

    public void setExportedFile(String exportedFile) {
        this.exportedFile = exportedFile;
    }
}