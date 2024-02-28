package pop;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.*;

public class ServerThread implements Runnable {
    final Socket socket;
    Scanner in;
    PrintWriter out;

    SessionState sessionState;
    String tmpEmail = null;

    User currentUser = null;
    MailRepository mailRepository = null;

    private enum SessionState {
        AUTHORIZATION,
        TRANSACTION,
        UPDATE
    }


    public ServerThread(Socket socket) {
        this.socket = socket;
        this.sessionState = SessionState.AUTHORIZATION;
    }

    @Override
    public void run() {
        try {
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream());

            // Send welcome message
            send("+OK POP3");

            // Handle requests
            while(socket.isConnected() && in.hasNextLine()){
                String plainRequest = in.nextLine();
                POPRequest request = new POPRequest(plainRequest);
                switch (sessionState){
                    case AUTHORIZATION:
                        handleUserAuthentication(request);
                        break;
                    case TRANSACTION:
                        handleUserTransactions(request);
                        break;
                }
            }
            // Update mails if needed
            if (sessionState.equals(SessionState.UPDATE)){
                mailRepository.updateMails();
            }
        } catch (IOException e) {
            System.err.println("[server]Error while initializing control socket: " + e);
        }
    }

    /**
     * Handle user authentication
     * @param request POPRequest
     * @throws IOException IOException
     */
    private void handleUserAuthentication(POPRequest request) throws IOException {
        switch (request.command){
            case USER:
                tmpEmail = request.args[0];
                send("+OK Send your password");
                break;
            case PASS:
                currentUser = authenticateUser(tmpEmail, request.args[0]);
                if (currentUser != null) {
                    mailRepository = new MailRepository(currentUser);
                }
                break;
            case QUIT:
                send("+OK POP3 server saying goodbye...");
                socket.close();
                break;
            case NOOP:
                send("+OK");
                break;
            case ERR:
                send("-ERR  Unknown method.");
                break;
            default:
                send("-ERR  Invalid command in current state.");
        }
    }

    /**
     * Authenticate user
     * @param email User email
     * @param password User password
     * @return User
     */
    private User authenticateUser(String email, String password) {
        // Check if user exists and password is correct
        if (
            email == null ||
            password == null ||
                    Server.userDontExists(email) ||
            !Objects.requireNonNull(Server.getUser(email)).getPassword().equals(password)
        ) {
            send("-ERR Invalid user name or password.");
            tmpEmail = null;
            return null;
        }
        // Check if user has already opened a session
        if (Server.userHasOpenedSession(email)) {
            send("-ERR Your mailbox is already locked");
            tmpEmail = null;
            return null;
        }
        // Authenticate user
        this.sessionState = SessionState.TRANSACTION;
        send("+OK Mailbox locked and ready");
        User user = Server.getUser(email);
        if (user != null) {
            user.hasOpenedSession = true;
            return user;
        }
        return null;
    }

    /**
     * Handle user transactions
     * @param request POPRequest
     * @throws IOException IOException
     */
    private void handleUserTransactions(POPRequest request) throws IOException {
        Map<Integer, Long> mailsSize;
        //long[] mailsSize;
        long totalSize;
        int index;
        String mail;
        switch (request.command){
            case STAT:
                mailsSize = mailRepository.getMailsSize();
                totalSize = 0;
                for(long mailSize : mailsSize.values()) {
                    totalSize += mailSize;
                }
                send("+OK " + mailsSize.size() + " " + totalSize);
                break;
            case LIST:
                if (request.args.length == 0) {
                    mailsSize = mailRepository.getMailsSize();
                    totalSize = 0;
                    for(long mailSize : mailsSize.values()) {
                        totalSize += mailSize;
                    }
                    send("+OK " + mailsSize.size() + " messages (" + totalSize + " octets)");
                    mailsSize.forEach((indexMail, sizeMail) -> send((indexMail + 1) + " " + sizeMail));
                    send(".");
                } else {
                    int mailIndex = Integer.parseInt(request.args[0]);
                    mailsSize = mailRepository.getMailsSize();
                    send("+OK " + mailIndex + " " + mailsSize.get(mailIndex));
                }
                break;
            case RETR:
                index = Integer.parseInt(request.args[0]);
                mail = mailRepository.getMailAtIndex(index);
                send("+OK " + mail.getBytes().length + " octets");
                send(mail);
                break;
            case TOP:
                index = Integer.parseInt(request.args[0]);
                mail = mailRepository.getMailAtIndex(index);
                StringReader reader = new StringReader(mail);
                Scanner scanner = new Scanner(reader);
                String line = scanner.nextLine();
                while (!line.equals("\n")) {
                    send(line);
                    line = scanner.nextLine();
                }
                int topLines = Integer.parseInt(request.args[1]);
                for (int i = 0; i < topLines; i++) {
                    send(line);
                    line = scanner.nextLine();
                }
                send(".");
                break;
            case DELE:
                int indexToDelete = Integer.parseInt(request.args[0]);
                mailRepository.deleteMail(indexToDelete - 1);
                send("+OK Message " + indexToDelete + " deleted");
                break;
            case RSET:
                mailRepository.resetDeletedMails();
                mailsSize = mailRepository.getMailsSize();
                totalSize = 0;
                for(long mailSize : mailsSize.values()) {
                    totalSize += mailSize;
                }
                send("+OK mailbox has " + mailsSize.size() + " messages (" + totalSize + " octets)");
                break;
            case NOOP:
                send("+OK");
                break;
            case QUIT:
                socket.close();
                sessionState = SessionState.UPDATE;
                send("+OK POP3 server saying goodbye...");
                break;
            case ERR:
                send("-ERR  Unknown method.");
                break;
            default:
                send("-ERR  Invalid command in current state.");
                break;
        }
    }

    /**
     * Send message to client
     * @param message Message to send
     */
    private void send(String message) {
        System.out.println(message);
        out.println(message);
        out.flush();
    }
}
