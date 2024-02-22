package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    ServerSocket serverSocket;
    ExecutorService pool;

    static ArrayList<User> users = new ArrayList<>();

    public Server()
    {
        try {
            serverSocket = new ServerSocket(110);
            pool = Executors.newFixedThreadPool(20);
        } catch (IOException e) {
            System.err.println("Error on creation of server socket:\n" + e);
        }
    }

    public void handleConnections()
    {
        try {
            while (true) {
                Socket newTask = serverSocket.accept();
                System.out.println("New client connected");
                pool.execute(new ServerThread(newTask));
            }
        } catch (IOException e) {
            System.err.println("Error while waiting for connection:\n" + e);
        }
    }

    public static void addUser(User user)
    {
        if (!userExists(user.getEmail())) users.add(user);
    }

    public static boolean userExists(String email)
    {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public static User getUser(String email)
    {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

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

