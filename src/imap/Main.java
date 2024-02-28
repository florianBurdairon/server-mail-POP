package imap;

public class Main {
    public static void main(String[] args) {
        // Create a new client
        Client client = new Client("localhost", 143);

        // Display all the unseen emails and delete them from an account
        client.process("alban@localhost", "alban");
    }
}
