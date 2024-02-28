package smtp;

public class Main {
    public static void main(String[] args) {
        // Create a new client
        Client client = new Client("localhost", 25);

        // Send some emails
        client.sendMail("localhost", "test@test", "alban@localhost", "Test 1", "Hello Test,\nThis is a test\uD83D\uDC49 \uD83D\uDE1A \uD83D\uDCAF \uD83D\uDE39 \uD83D\uDC40 message.");
        client.sendMail("localhost", "test@test", "alban@localhost", "Test 2", "Hello \uD83D\uDC49 \uD83D\uDE1A \uD83D\uDCAF \uD83D\uDE39 \uD83D\uDC40Test 2,This is another test message.");
        client.sendMail("localhost", "test@test", "alban@localhost", "Test 3", "Hello Test 3,This is a test\uD83D\uDC49 \uD83D\uDE1A \uD83D\uDCAF \uD83D\uDE39 \uD83D\uDC40 message.");
        client.sendMail("localhost", "test@test", "alban@localhost", "Test 4", "Hello Test 12,\uD83D\uDC49 \uD83D\uDE1A \uD83D\uDCAF \uD83D\uDE39 \uD83D\uDC40This is a test message.");
    }
}