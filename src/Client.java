import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {

    String ipServer;
    int portSMTP = 25;
    int portPOP = 110;
    int portIMAP = 143;
    private Scanner in;
    private PrintWriter out;

    private static Map<String,String> map = new HashMap<>();

    public Client(String ipServer) {
        map.put("Alban","alban@test");
        map.put("Florian","florian@test");
        map.put("Thomas","thomas@test");
        
        this.ipServer = ipServer;
    }


    public void deleteEveryMailForUser(String username, String password) {
        System.out.println("\n===========================");
        System.out.println("= READING MAILS           =");
        System.out.println("===========================\n");
        try {
            Socket popSocket = new Socket(ipServer, portPOP);

            if (popSocket.isConnected()) {
                in = new Scanner(popSocket.getInputStream());
                out = new PrintWriter(popSocket.getOutputStream());

                String response = in.nextLine();
                System.out.println(response);

                try {
                    sendPop(in, out, "USER " + username);
                    sendPop(in, out, "PASS " + password);
                    sendPop(in, out, "LIST");
                    String line = in.nextLine();
                    ArrayList<String> mailIndexes = new ArrayList<>();
                    while (!line.equals(".")) {
                        mailIndexes.add(line.split(" ")[0]);
                        System.out.println(line);
                        line = in.nextLine();
                    }
                    mailIndexes.forEach((index) -> {
                        try {
                            sendPop(in, out, "DELE " + index);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    });
                    sendPop(in, out, "QUIT");
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            } else {
                System.err.println("Could not connect :(");
            }
        } catch (IOException e) {
            System.err.println("Error on connection to Server: " + e);
        }
    }


    public void sendMailToList() {
        map.forEach((name, email) -> {
            sendMail("test", "test@test", email, "Test", "Hello " + name + ",\n\nThis is a test message.");
        });
    }

    public void sendMail(String ip, String fromEmail, String toEmail, String subject, String message) {
        System.out.println("Sending mail to " + toEmail);

        try {
            Socket smtpSocket = new Socket(ipServer, portSMTP);
            
            if (smtpSocket.isConnected()) {
                in = new Scanner(smtpSocket.getInputStream());
                out = new PrintWriter(smtpSocket.getOutputStream());
                
                String response = in.nextLine();
                System.out.println(response);

                try {
                    send(in, out, "HELO " + ip, true);
                    send(in, out, "MAIL FROM: " + fromEmail, true);
                    send(in, out, "RCPT TO: " + toEmail, true);
                    send(in, out, "DATA", true);
                    send(in, out, "Subject: " + subject, false);
                    send(in, out, "", false);
                    send(in, out, message, false);
                    send(in, out, ".", true);
                    send(in, out, "QUIT", true);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            } else {
                System.err.println("Could not connect :(");
            }
        } catch (IOException e) {
            System.err.println("Error on connection to Server: " + e);
        }
    }

    private static void send(Scanner in, PrintWriter out, String message, boolean requiresResponse) throws Exception {
        System.out.println(message);
        out.println(message);
        out.flush();
        if (requiresResponse) {
            String response = in.nextLine();
            System.out.println(response);
            if (!(response.contains("354") || response.matches("^2\\d\\d.+$"))) {
                throw new Exception("Server responded: '" + response + "' when sending '" + message + "'");
            }
        }
    }

    private static String sendPop(Scanner in, PrintWriter out, String message) throws Exception {
        System.out.println(message);
        out.println(message);
        out.flush();
        String response = in.nextLine();
        System.out.println(response);
        if (response.contains("-ERR")) {
            throw new Exception("Server responded: '" + response + "' when sending '" + message + "'");
        }
        return response;
    }
}
