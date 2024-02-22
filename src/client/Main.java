package client;

public class Main {
    public static void main(String[] args) {
        Client client = new Client("localhost");
        client.sendMail("test", "test@test", "florian@test", "Test 1", "Hello Test,\nThis is a test\uD83D\uDC49 \uD83D\uDE1A \uD83D\uDCAF \uD83D\uDE39 \uD83D\uDC40 message.");
        client.sendMail("test", "test@test", "florian@test", "Test 2", "Hello \uD83D\uDC49 \uD83D\uDE1A \uD83D\uDCAF \uD83D\uDE39 \uD83D\uDC40Test 2,This is another test message.");
        client.sendMail("test", "test@test", "florian@test", "Test 3", "Hello Test 3,This is a test\uD83D\uDC49 \uD83D\uDE1A \uD83D\uDCAF \uD83D\uDE39 \uD83D\uDC40 message.");
        client.sendMail("test", "test@test", "florian@test", "Test 4", "Hello Test 12,\uD83D\uDC49 \uD83D\uDE1A \uD83D\uDCAF \uD83D\uDE39 \uD83D\uDC40This is a test message.");
    }
}