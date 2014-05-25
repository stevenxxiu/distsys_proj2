package simplestream;

<<<<<<< HEAD
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
=======
import org.bridj.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
>>>>>>> 8776f3f1d988350d2506496f5033ba509abe244e
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
<<<<<<< HEAD
	public int port;
	public int maxClients;
	public int rateLimit;
	public boolean isLocal;
	List<Thread> serverThreads = new ArrayList<Thread>();

	public Server(int port, boolean isLocal, int rateLimit, int maxClients) {
		this.port = port;
		this.maxClients = maxClients;
		this.rateLimit = rateLimit;
		this.isLocal = isLocal;
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
			JSONObject status = new JSONObject();

			try {
				status.put("response", "status");
				status.put("streaming", isLocal ? "local" : "remote");
				status.put("clients", serverThreads.size());
				status.put("ratelimiting", "yes");
				status.put("handover", "yes");
			} catch (JSONException e) {
				assert false;
			}
			try {
				output.writeUTF(status.toString());
				output.flush();
				// clientSocket.close();
			} catch (IOException e) {
				System.out.println("Client exited");
				continue;
			}
			Thread serverThread = new Thread(new ServerThread(clientSocket,
					input, output, rateLimit, this));
			serverThreads.add(serverThread);
			serverThread.start();
		}
	}

	public static void main(String[] args) {

		Server server = new Server(6262, true, 100, 3);
		server.start();

	}
=======
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
>>>>>>> 8776f3f1d988350d2506496f5033ba509abe244e
}
