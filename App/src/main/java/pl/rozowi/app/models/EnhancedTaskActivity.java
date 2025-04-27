package pl.rozowi.app.models;

import java.sql.Timestamp;

/**
 * Rozszerzona wersja klasy TaskActivity, która zawiera dodatkowe informacje
 * potrzebne do efektywnego wyświetlania aktywności w interfejsie użytkownika
 */
public class EnhancedTaskActivity extends TaskActivity {

    private String taskTitle;
    private String userName;
    private String userLastName;
    private String userEmail;

    // Konstruktor kopiujący z podstawowej klasy TaskActivity
    public EnhancedTaskActivity(TaskActivity baseActivity) {
        this.setId(baseActivity.getId());
        this.setTaskId(baseActivity.getTaskId());
        this.setUserId(baseActivity.getUserId());
        this.setActivityType(baseActivity.getActivityType());
        this.setDescription(baseActivity.getDescription());
        this.setActivityDate(baseActivity.getActivityDate());
    }

    public EnhancedTaskActivity() {
        // Domyślny konstruktor
    }

    // Gettery i settery dla nowych pól

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Zwraca pełne imię i nazwisko użytkownika
     * @return String w formacie "Imię Nazwisko"
     */
    public String getFullUserName() {
        if (userName != null && userLastName != null) {
            return userName + " " + userLastName;
        } else if (userName != null) {
            return userName;
        } else if (userLastName != null) {
            return userLastName;
        } else {
            return "Nieznany użytkownik (ID: " + getUserId() + ")";
        }
    }

    /**
     * Zwraca informacje o użytkowniku w formie przyjaznej do wyświetlenia
     * @return String w formacie "Imię Nazwisko (email)"
     */
    public String getUserDisplayString() {
        String name = getFullUserName();
        if (userEmail != null && !userEmail.isEmpty()) {
            return name + " (" + userEmail + ")";
        } else {
            return name;
        }
    }

    /**
     * Zwraca tytuł zadania lub informację o braku zadania
     * @return Tytuł zadania lub informacja o braku
     */
    public String getTaskTitleOrDefault() {
        // Dla aktywności systemowych (taskId = 0) nie wyświetlamy zadania
        if (getTaskId() == 0) {
            return "Zdarzenie systemowe";
        }

        // Dla aktywności na zadaniach zwracamy tytuł lub ID
        if (taskTitle != null && !taskTitle.isEmpty()) {
            return taskTitle;
        } else {
            return "Zadanie ID: " + getTaskId();
        }
    }

    /**
     * Przetwarza typ aktywności na bardziej przyjazną dla użytkownika nazwę
     * @return Nazwa typu aktywności w języku polskim
     */
    public String getActivityTypeDisplayName() {
        String type = getActivityType();
        if (type == null) return "Nieznany";

        switch (type) {
            case "CREATE":
                return "Utworzenie";
            case "UPDATE":
                return "Aktualizacja";
            case "STATUS":
                return "Zmiana statusu";
            case "ASSIGN":
                return "Przypisanie";
            case "COMMENT":
                return "Komentarz";
            case "PASSWORD":
                return "Zmiana hasła";
            case "LOGIN":
                return "Logowanie";
            case "LOGOUT":
                return "Wylogowanie";
            case "USER_MANAGEMENT":
                return "Zarządzanie użytkownikami";
            case "TEAM_MANAGEMENT":
                return "Zarządzanie zespołami";
            default:
                return type;
        }
    }

    /**
     * Formatuje datę aktywności do wyświetlenia
     * @return Sformatowana data i czas
     */
    public String getFormattedDate() {
        Timestamp date = getActivityDate();
        if (date == null) return "";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * Określa, czy aktywność jest systemowa czy związana z zadaniem
     * @return true dla zdarzeń systemowych, false dla aktywności związanych z zadaniami
     */
    public boolean isSystemEvent() {
        return getTaskId() == 0;
    }

    /**
     * Zwraca dodatkowe klasy CSS odpowiednie dla danego typu aktywności
     * dla użycia w stylowaniu UI
     * @return String z klasami CSS
     */
    public String getActivityCssClasses() {
        String type = getActivityType();
        if (type == null) return "activity-unknown";

        switch (type.toUpperCase()) {
            case "CREATE":
                return "activity-create";
            case "UPDATE":
                return "activity-update";
            case "STATUS":
                return "activity-status";
            case "ASSIGN":
                return "activity-assign";
            case "COMMENT":
                return "activity-comment";
            case "PASSWORD":
                return "activity-password";
            case "LOGIN":
                return "activity-login";
            case "LOGOUT":
                return "activity-logout";
            case "USER_MANAGEMENT":
                return "activity-user-management";
            case "TEAM_MANAGEMENT":
                return "activity-team-management";
            default:
                return "activity-other";
        }
    }
}