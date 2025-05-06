package pl.rozowi.app.reports;

import com.itextpdf.text.BaseColor;

public interface ReportStyle {
    BaseColor getPrimaryColor();
    BaseColor getSecondaryColor();
    BaseColor getLightColor();
    String getReportFooterText();
    String getPageNumberPrefix();
}