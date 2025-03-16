package pl.rozowi.app.models;

public class User {
    private int id;
    private String name;
    private String lastName;
    private String password;
    private String email;
    private int roleId;
    private String stanowisko;
    private String passwordHint;

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public String getStanowisko() { return stanowisko; }
    public void setStanowisko(String stanowisko) { this.stanowisko = stanowisko; }
    public String getPasswordHint() { return passwordHint; }
    public void setPasswordHint(String passwordHint) { this.passwordHint = passwordHint; }
}
