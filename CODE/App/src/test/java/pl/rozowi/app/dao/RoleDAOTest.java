package pl.rozowi.app.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Role;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Testy jednostkowe dla klasy RoleDAO, odpowiedzialnej za operacje
 * na rolach użytkowników w bazie danych.
 */
public class RoleDAOTest {

    private Connection connection;
    private RoleDAO roleDAO;

    /**
     * Konfiguracja środowiska testowego przed każdym testem.
     * Tworzy tymczasową bazę danych H2 w pamięci oraz tabelę roles
     * z dwoma przykładowymi rolami: Admin i User.
     */
    @Before
    public void setUp() throws Exception {
        DatabaseManager.setTestUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        connection = DatabaseManager.getConnection();

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE roles ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "role_name VARCHAR(255),"
                    + "permissions VARCHAR(1024)"
                    + ")");

            stmt.execute("INSERT INTO roles (role_name, permissions) VALUES ('Admin', 'ALL')");
            stmt.execute("INSERT INTO roles (role_name, permissions) VALUES ('User', 'READ')");
        }

        roleDAO = new RoleDAO();
    }

    /**
     * Czyszczenie zasobów po wykonaniu każdego testu.
     * Usuwa wszystkie tabele z bazy danych i zamyka połączenie.
     */
    @After
    public void tearDown() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        connection.close();
        DatabaseManager.setTestUrl(null);
    }

    /**
     * Test sprawdzający metodę getAllRoles, która powinna zwrócić wszystkie
     * role z bazy danych. Weryfikuje, czy wszystkie pola ról są prawidłowo
     * odczytane, włącznie z identyfikatorami, nazwami i uprawnieniami.
     */
    @Test
    public void testGetAllRoles() {
        List<Role> roles = roleDAO.getAllRoles();

        assertNotNull(roles);
        assertEquals(2, roles.size());

        Role first = roles.get(0);
        assertEquals(1, first.getId());
        assertEquals("Admin", first.getRoleName());
        assertEquals("ALL", first.getPermissions());

        Role second = roles.get(1);
        assertEquals(2, second.getId());
        assertEquals("User", second.getRoleName());
        assertEquals("READ", second.getPermissions());
    }

    /**
     * Test sprawdzający metodę getRoleByName dla istniejącej roli.
     * Powinien zwrócić poprawny obiekt Role dla nazwy "Admin".
     */
    @Test
    public void testGetRoleByName_found() {
        Role role = roleDAO.getRoleByName("Admin");

        assertNotNull(role);
        assertEquals(1, role.getId());
        assertEquals("Admin", role.getRoleName());
    }

    /**
     * Test sprawdzający metodę getRoleByName dla nieistniejącej roli.
     * Powinien zwrócić null, gdy rola o podanej nazwie nie istnieje.
     */
    @Test
    public void testGetRoleByName_notFound() {
        Role role = roleDAO.getRoleByName("DoesNotExist");

        assertNull(role);
    }

    /**
     * Test sprawdzający poprawne dodanie nowej roli do bazy danych.
     * Tworzy rolę "Guest", dodaje ją do bazy i weryfikuje, czy została
     * poprawnie zapisana poprzez próbę odczytu jej po nazwie.
     */
    @Test
    public void testInsertRole_success() {
        Role r = new Role();
        r.setRoleName("Guest");
        r.setPermissions("GUEST_PERMS");

        boolean inserted = roleDAO.insertRole(r);

        assertTrue(inserted);

        Role insertedRole = roleDAO.getRoleByName("Guest");
        assertNotNull(insertedRole);
        assertEquals("Guest", insertedRole.getRoleName());
    }

    /**
     * Test sprawdzający reakcję na próbę dodania roli o nazwie, która już istnieje.
     * Najpierw dodaje rolę "Guest" bezpośrednio do bazy, dodając ograniczenie unique,
     * a następnie próbuje dodać kolejną rolę o tej samej nazwie poprzez RoleDAO.
     * Metoda insertRole powinna zwrócić false, sygnalizując niepowodzenie operacji.
     */
    @Test
    public void testInsertRole_failure() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE roles ADD CONSTRAINT unique_role_name UNIQUE (role_name)");
            stmt.execute("INSERT INTO roles (role_name) VALUES ('Guest')");
        }

        Role r = new Role();
        r.setRoleName("Guest");

        boolean inserted = roleDAO.insertRole(r);

        assertFalse(inserted);
    }
}