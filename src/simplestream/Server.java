package simplestream;

import org.bridj.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public int sport;
    public int maxClients;
    public int rateLimit;
    public String rhost;
    public Integer rport;
    public ImageReceiverInterface receiver;
    List<Pair<Thread, ServerThread>> serverThreads;

    public Server(int sport, String rhost, Integer rport, ImageReceiverInterface receiver, int rateLimit, int maxClients) {
        this.sport = sport;
        this.maxClients = maxClients;
        this.rateLimit = rateLimit;
        this.rhost = rhost;
        this.rport = rport;
        this.receiver = receiver;
        serverThreads = new ArrayList<Pair<Thread, ServerThread>>();
    }

    public boolean isOverloaded() {
        // check for which threads have exited
        List<Pair<Thread, ServerThread>> serverThreadsAlive = new ArrayList<Pair<Thread, ServerThread>>();
        for (Pair<Thread, ServerThread> pair : serverThreads) {
            if (pair.getFirst().isAlive())
                serverThreadsAlive.add(pair);
        }
        serverThreads = serverThreadsAlive;
        return serverThreads.size() > maxClients;
    }

    public void start() {
        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(sport);
        } catch (IOException e) {
            System.out.println("Could not listen on port " + sport);
            return;
        }
        System.out.println("Server Started");
        while (true) {
            BufferedWriter output;
            Socket clientSocket;
            try {
                clientSocket = listenSocket.accept();
                output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            } catch (IOException e) {
                System.out.println("Client exited");
                continue;
            }
            System.out.println("Client connected");
            System.out.println("Sending status response");
            JSONObject status = null;
            try {
                try {
                    ServerStatus serverStatus = new ServerStatus(rport == null, serverThreads.size(), true, true);
                    status = serverStatus.toJSON();
                    status.put("response", "status");
                } catch (JSONException e) {
                    assert false;
                }
                output.write(status.toString() + "\n");
                output.flush();
            } catch (IOException e) {
                System.out.println("Client exited");
                continue;
            }
            ServerThread serverRunnable = new ServerThread(clientSocket, rateLimit, receiver, this);
            Thread serverThread = new Thread(serverRunnable);
            serverThreads.add(new Pair<Thread, ServerThread>(serverThread, serverRunnable));
            serverThread.start();
        }
    }
}
