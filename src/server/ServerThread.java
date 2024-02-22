package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.*;

public class ServerThread implements Runnable {
    Socket socket;
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

            send("+OK POP3");
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
            if (sessionState.equals(SessionState.UPDATE)){
                mailRepository.updateMails();
            }
        } catch (IOException e) {
            System.err.println("[server]Error while initializing control socket: " + e);
        }
    }

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
            default:
                send("-ERR  Invalid command in current state.");
        }
    }

    private User authenticateUser(String email, String password) {
        // Check if user exists and password is correct
        if (
            email == null ||
            password == null ||
            !Server.userExists(email) ||
            !Server.getUser(email).getPassword().equals(password)
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
        user.hasOpenedSession = true;
        return user;
    }

    private void handleUserTransactions(POPRequest request) throws IOException {
        long[] mailsSize;
        long totalSize;
        int index;
        String mail;
        switch (request.command){
            case STAT:
                mailsSize = mailRepository.getMailsSize();
                totalSize = 0;
                for(long mailSize : mailsSize) {
                    totalSize += mailSize;
                }
                send("+OK " + mailsSize.length + " " + totalSize);
                break;
            case LIST:
                if (request.args.length == 0) {
                    mailsSize = mailRepository.getMailsSize();
                    totalSize = 0;
                    for(long mailSize : mailsSize) {
                        totalSize += mailSize;
                    }
                    send("+OK " + mailsSize.length + " messages (" + totalSize + " octets)");
                    for (int i = 0; i < mailsSize.length; i++) {
                        send((i + 1) + " " + mailsSize[i]);
                    }
                    send(".");
                } else {
                    int mailIndex = Integer.parseInt(request.args[0]);
                    long[] mailSize = mailRepository.getMailsSize();
                    send("+OK " + mailIndex + " " + mailSize[mailIndex]);
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
                //FIXME: Behavior not expected
                int indexToDelete = Integer.parseInt(request.args[0]);
                mailRepository.deleteMail(indexToDelete);
                send("+OK Message " + indexToDelete + " deleted");
                break;
            case RSET:
                mailRepository.resetDeletedMails();
                mailsSize = mailRepository.getMailsSize();
                totalSize = 0;
                for(long mailSize : mailsSize) {
                    totalSize += mailSize;
                }
                send("+OK maildrop has " + mailsSize.length + " messages (" + totalSize + " octets)");
                break;
            case NOOP:
                send("+OK");
                break;
            case QUIT:
                socket.close();
                sessionState = SessionState.UPDATE;
                send("+OK POP3 server saying goodbye...");
                break;
            default:
                send("-ERR  Invalid command in current state.");
                break;
        }
    }

    private void send(String message) {
        System.out.println(message);
        out.println(message);
        out.flush();
    }
}
