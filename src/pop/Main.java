package pop;

public class Main {
    public static void main(String[] args) {
        // Add users
        Server.addUser(new User("florian@test", "florian"));
        Server.addUser(new User("alban@test", "alban"));
        Server.addUser(new User("thomas@test", "thomas"));

        // Start server
        Server server = new Server(110);
        server.handleConnections();
    }
}