package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public boolean insertReport(Report report) {
        String sql = "INSERT INTO reports (report_name, report_scope, details) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, report.getReportName());
            stmt.setString(2, report.getReportScope());
            stmt.setString(3, report.getDetails());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setReportName(rs.getString("report_name"));
                report.setReportScope(rs.getString("report_scope"));
                report.setDetails(rs.getString("details"));
                reports.add(report);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return reports;
    }
}
