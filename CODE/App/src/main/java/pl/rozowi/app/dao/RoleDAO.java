package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object dla zarządzania rolami użytkowników w systemie.
 * Zapewnia podstawowe operacje CRUD na tabeli ról w bazie danych.
 */
public class RoleDAO {

    /**
     * Pobiera wszystkie role z bazy danych.
     *
     * @return Lista obiektów {@link Role} reprezentujących wszystkie role w systemie.
     *         Pusta lista jeśli nie znaleziono żadnych ról.
     */
    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT id, role_name, permissions FROM roles";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setId(rs.getInt("id"));
                role.setRoleName(rs.getString("role_name"));
                role.setPermissions(rs.getString("permissions"));
                roles.add(role);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return roles;
    }

    /**
     * Wyszukuje rolę na podstawie nazwy.
     *
     * @param roleName Nazwa roli do wyszukania
     * @return Obiekt {@link Role} jeśli znaleziono, null w przeciwnym przypadku
     */
    public Role getRoleByName(String roleName) {
        Role role = null;
        String sql = "SELECT * FROM roles WHERE role_name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                role = new Role();
                role.setId(rs.getInt("id"));
                role.setRoleName(rs.getString("role_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return role;
    }

    /**
     * Dodaje nową rolę do bazy danych.
     *
     * @param role Obiekt {@link Role} zawierający nazwę nowej roli
     * @return true jeśli rola została pomyślnie dodana, false w przypadku błędu
     */
    public boolean insertRole(Role role) {
        String sql = "INSERT INTO roles (role_name) VALUES (?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.getRoleName());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}