package pop;

public class User {
    private final String email;
    private final String password;
    private final String username;
    public boolean hasOpenedSession;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        username = email.split("@")[0];
        hasOpenedSession = false;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
