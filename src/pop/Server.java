package pop;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    ServerSocket serverSocket;
    ExecutorService pool;

    final static ArrayList<User> users = new ArrayList<>();

    public Server(int port)
    {
        try {
            // Create server socket
            serverSocket = new ServerSocket(port);
            pool = Executors.newFixedThreadPool(20);
        } catch (IOException e) {
            System.err.println("Error on creation of server socket:\n" + e);
        }
    }

    public void handleConnections()
    {
        while (true) {
            // Wait for new connection
            try (Socket newTask = serverSocket.accept()) {
                System.out.println("New client connected");

                // Start new thread for the new connection
                pool.execute(new ServerThread(newTask));
            } catch (IOException e) {
                System.err.println("Error while waiting for connection:\n" + e);
                break;
            }
        }
    }

    /**
     * Add a user to the server
     * @param user User to add
     */
    public static void addUser(User user)
    {
        if (userDontExists(user.getEmail())) users.add(user);
    }

    /**
     * Check if a user with the given email exists
     * @param email Email of the user
     * @return boolean
     */
    public static boolean userDontExists(String email)
    {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the user with the given email
     * @param email Email of the user
     * @return User
     */
    public static User getUser(String email)
    {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Check if the user with the given email has opened a session
     * @param email Email of the user
     * @return boolean
     */
    public static boolean userHasOpenedSession(String email)
    {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u.hasOpenedSession;
            }
        }
        return false;
    }
}

