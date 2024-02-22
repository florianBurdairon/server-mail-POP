package server;

public class Main {
    public static void main(String[] args) {
        Server.addUser(new User("florian@test", "florian"));
        Server.addUser(new User("alban@test", "alban"));
        Server.addUser(new User("thomas@test", "thomas"));
        Server server = new Server();
        server.handleConnections();
    }
}