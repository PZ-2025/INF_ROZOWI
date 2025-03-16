package pl.rozowi.app.models;

public class Report {
    private int id;
    private String reportName;
    private String reportScope;
    private String details;

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
    public String getReportScope() {
        return reportScope;
    }
    public void setReportScope(String reportScope) {
        this.reportScope = reportScope;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }
}
