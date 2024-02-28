package imap;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    final String ipServer;
    final int portIMAP;

    public Client(String ipServer, int port) {
        this.ipServer = ipServer;
        this.portIMAP = port;
    }

    public void process(String email, String password) {
        //Connect to the IMAP server
        try (Socket imapSocket = new Socket(ipServer, portIMAP)) {
            if (imapSocket.isConnected()) {
                Scanner in = new Scanner(imapSocket.getInputStream());
                PrintWriter out = new PrintWriter(imapSocket.getOutputStream());

                String response = in.nextLine();
                System.out.println(response);
                try {
                    char commandIndex = 'a';
                    //Send the LOGIN commands to the server with the email and password
                    send(in, out,  commandIndex++ + " LOGIN " + email + " " + password);

                    //Send the SELECT command to the server to use the INBOX mailbox
                    send(in, out, commandIndex++ + " SELECT INBOX");

                    //Get all the unseen mails and delete them
                    //Send the SEARCH command to the server to get the list of unseen mails
                    String mails = sendAndGetResponse(in, out, commandIndex++ + " SEARCH UNSEEN");
                    System.out.println(mails);
                    if (!mails.contains("* SEARCH\n")) {
                        String[] mailsIds = mails.split("\n")[0].replace("* SEARCH ", "").split(" ");
                        for (String mailID : mailsIds) {
                            //Send the FETCH command to the server to get the mail
                            String mail = sendAndGetResponse(in, out, commandIndex++ + " FETCH " + mailID + " (BODY[])");
                            System.out.println(mail);
                            //Send the STORE command to the server to delete the mail
                            send(in, out, commandIndex++ + " STORE " + mailID + " +FLAGS (\\Deleted)");
                        }
                    }
                    //Send the CLOSE and LOGOUT commands to the server to close the connection and delete the mails
                    send(in, out, commandIndex++ + " CLOSE");
                    send(in, out, commandIndex + " LOGOUT");
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

    private void send(Scanner in, PrintWriter out, String message) throws Exception {
        // Send message to server
        System.out.println(message);
        out.println(message);
        out.flush();

        // Get response from server
        String response;
        do {
            response = in.nextLine();
            System.out.println(response);
            if (response.contains("BAD") || response.contains("NO")) {
                throw new Exception("Server responded: '" + response + "' when sending '" + message + "'");
            }
        } while (response.charAt(0) != message.charAt(0));
    }

    private String sendAndGetResponse(Scanner in, PrintWriter out, String message) {
        // Send message to server
        System.out.println(message);
        out.println(message);
        out.flush();

        // Get response from server
        StringBuilder total = new StringBuilder();
        String response;
        do {
            response = in.nextLine() + "\n";
            total.append(response);
        } while (response.charAt(0) != message.charAt(0));
        return total.toString();
    }
}