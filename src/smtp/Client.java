package smtp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    final String ipServer;
    final int portSMTP;

    public Client(String ipServer, int port) {
        this.ipServer = ipServer;
        this.portSMTP = port;
    }

    public void sendMail(String ip, String fromEmail, String toEmail, String subject, String message) {
        System.out.println("Sending mail to " + toEmail);

        // Create a socket to the server
        try (Socket smtpSocket = new Socket(ipServer, portSMTP)) {
            if (smtpSocket.isConnected()) {
                Scanner in = new Scanner(smtpSocket.getInputStream());
                PrintWriter out = new PrintWriter(smtpSocket.getOutputStream());

                String response = in.nextLine();
                System.out.println(response);

                // Send the commands to the server
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
        // Send the message to the server
        System.out.println(message);
        out.println(message);
        out.flush();

        // Get the response from the server
        if (requiresResponse) {
            String response = in.nextLine();
            System.out.println(response);
            if (!(response.contains("354") || response.matches("^2\\d\\d.+$"))) {
                throw new Exception("Server responded: '" + response + "' when sending '" + message + "'");
            }
        }
    }
}

