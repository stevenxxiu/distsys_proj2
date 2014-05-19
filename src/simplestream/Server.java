package simplestream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public int port;
    public int maxClients;
    public int rateLimit;
    public boolean isLocal;
    public ImageReceiverInterface receiver;
    List<Thread> serverThreads = new ArrayList<Thread>();

    public Server(int port, ImageReceiverInterface receiver, boolean isLocal, int rateLimit, int maxClients) {
        this.port = port;
        this.maxClients = maxClients;
        this.rateLimit = rateLimit;
        this.isLocal = isLocal;
        this.receiver = receiver;
    }

    public boolean isOverloaded() {
        // check for which threads have exited
        List<Thread> serverThreadsAlive = new ArrayList<Thread>();
        for (Thread thread : serverThreads) {
            if (thread.isAlive())
                serverThreadsAlive.add(thread);
        }
        serverThreads = serverThreadsAlive;
        return serverThreads.size() > maxClients;
    }

    public void start() {
        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Could not listen on port " + port);
            return;
        }
        System.out.println("Server Started");
        while (true) {
            DataInputStream input;
            DataOutputStream output;
            Socket clientSocket;
            try {
                clientSocket = listenSocket.accept();
                input = new DataInputStream(clientSocket.getInputStream());
                output = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println("Client exited");
                continue;
            }
            System.out.println("Client connected");
            JSONObject status = null;
            try {
                ServerStatus serverStatus = new ServerStatus(isLocal, serverThreads.size(), true, true);
                status = serverStatus.toJSON();
                status.put("response", "status");
            } catch (JSONException e) {
                assert false;
            }
            try {
                output.writeUTF(status.toString());
                output.flush();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Client exited");
                continue;
            }
            Thread serverThread = new Thread(new ServerThread(clientSocket, input, output, rateLimit, receiver, this));
            serverThreads.add(serverThread);
            serverThread.start();
        }
    }
}
