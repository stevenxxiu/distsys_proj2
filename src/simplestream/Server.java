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
    public boolean isLocal;
    List<Thread> serverThreads = new ArrayList<Thread>();

    public Server(int port, boolean isLocal, int maxClients){
        this.port = port;
        this.maxClients = maxClients;
        this.isLocal = isLocal;
    }

    public void start(){
        ServerSocket listenSocket;

        try {
            listenSocket = new ServerSocket(port);
        }catch(IOException e){
            System.out.println("Could not listen on port " + port);
            return;
        }
        System.out.println("Server Started");
        while(true){
            DataInputStream input;
            DataOutputStream output;
            Socket clientSocket;
            try{
                clientSocket = listenSocket.accept();
                input = new DataInputStream(clientSocket.getInputStream());
                output = new DataOutputStream(clientSocket.getOutputStream());
            }catch(IOException e){
                System.out.println("Client exited");
                continue;
            }
            System.out.println("Client connected");
            JSONObject status = new JSONObject();
            try{
                status.put("response", "status");
                status.put("streaming", isLocal?"local":"remote");
                status.put("clients", serverThreads.size());
                status.put("ratelimiting", "yes");
                status.put("handover", "yes");
            }catch(JSONException e){
                assert false;
            }
            try{
                output.writeUTF(status.toString());
                output.flush();
                clientSocket.close();
            }catch(IOException e){
                System.out.println("Client exited");
                continue;
            }
            Thread serverThread = new Thread(new ServerThread(input, output));
            serverThreads.add(serverThread);
            serverThread.start();
        }
    }
}
