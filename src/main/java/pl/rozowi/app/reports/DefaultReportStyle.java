package pl.rozowi.app.reports;

import com.itextpdf.text.BaseColor;

public class DefaultReportStyle implements ReportStyle {
    @Override
    public BaseColor getPrimaryColor() {
        return new BaseColor(0, 123, 255);
    }

    @Override
    public BaseColor getSecondaryColor() {
        return new BaseColor(30, 30, 47);
    }

    @Override
    public BaseColor getLightColor() {
        return new BaseColor(240, 240, 245);
    }

    @Override
    public String getReportFooterText() {
        return "Ten raport został wygenerowany automatycznie przez system TaskApp.";
    }

    @Override
    public String getPageNumberPrefix() {
        return "TaskApp - Strona ";
    }
}