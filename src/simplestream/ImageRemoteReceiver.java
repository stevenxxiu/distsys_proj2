package simplestream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageRemoteReceiver implements ImageReceiverInterface {
    byte[] image;
    int sport;
	int port;
	String hostname;
    int rateLimit;
    // list of dead servers, never refreshed
    HashSet<InetSocketAddress> serversDead;
    // list of servers to search for in case the current one is dead
    ArrayBlockingQueue<InetSocketAddress> serversQueue;

    public ImageRemoteReceiver(int sport, int port, String hostname, int rateLimit) {
        this.sport = sport;
		this.port = port;
		this.hostname = hostname;
        this.rateLimit = rateLimit;
	}

    class ReceiverThread implements Runnable {
        public void run(){
            Socket clientSocket;
            try {
                clientSocket = new Socket(InetAddress.getByName(hostname), port);
            }catch(IOException e){
                System.out.println("Could not connect to server: " + hostname + ":" + port);
                return;
            }
            DataInputStream input;
            DataOutputStream output;
            JSONObject request, response;
            try {
                input = new DataInputStream(clientSocket.getInputStream());
                output = new DataOutputStream(clientSocket.getOutputStream());
                ServerStatus serverStatus;
                // read the server's status response
                response = new JSONObject(input.readUTF());
                if (response.getString("response").equals("status")) {
                    serverStatus = ServerStatus.fromJSON(response);
                } else {
                    System.out.println("Invalid response: " + response.getString("response"));
                    return;
                }
                // send startstream request
                request = new JSONObject();
                try {
                    request.put("request", "startstream");
                    request.put("sport", sport);
                    // send rate limiting request if possible
                    if (serverStatus.hasRateLimiting) {
                        request.put("ratelimit", rateLimit);
                    }
                } catch (JSONException e) {
                    assert false;
                }
                output.writeUTF(request.toString());
                output.flush();
                // read server's response
                response = new JSONObject(input.readUTF());
                if (response.getString("response").equals("startingstream")) {
                } else if (response.getString("response").equals("overloaded")) {
                    System.out.println("Server overloaded");
                    if (serverStatus.hasHandOver && !serverStatus.isLocal){
                        // try to connect to another server by searching using BFS
                        InetSocketAddress address;
                        JSONArray handoverClients = response.getJSONArray("clients");
                        JSONObject handoverServer = response.getJSONObject("clients");
                        System.out.println("Handover data is present, searching for other servers using BFS");
                        for(int i=0;i<handoverClients.length();i++){
                            JSONObject handoverClient = handoverClients.getJSONObject(i);
                            address = new InetSocketAddress(handoverClient.getString("ip"), handoverClient.getInt("port"));
                            if(!serversDead.contains(address))
                                serversQueue.add(address);
                        }
                        address = new InetSocketAddress(handoverServer.getString("ip"), handoverServer.getInt("port"));
                        if(!serversDead.contains(address))
                            serversQueue.add(address);
                    }
                    return;
                } else {
                    System.out.println("Invalid response: " + response.getString("response"));
                    return;
                }
                while (true) {
                    try {
                        response = new JSONObject(input.readUTF());
                        if (response.getString("response").equals("image")) {
                            image = Compressor.decompress(Base64.decode(response.getString("data")));
                        } else {
                            System.out.println("Client didn't receive an image response");
                            return;
                        }
                    } catch (InterruptedIOException e) {
                        input.reset();
                        // send stopstream request
                        request = new JSONObject();
                        try {
                            request.put("request", "stopstream");
                            request.put("sport", sport);
                        } catch (JSONException e_) {
                            assert false;
                        }
                        output.writeUTF(request.toString());
                        output.flush();
                        // read server's response
                        response = new JSONObject(input.readUTF());
                        if (response.getString("response").equals("stoppedstream")) {
                        } else {
                            System.out.println("Invalid response: " + response.getString("response"));
                            return;
                        }
                        return;
                    }
                }
            } catch (InterruptedIOException e){
                System.out.println("Transmissions not finished yet, client force exited");
                return;
            } catch (IOException e) {
                System.out.println("Disconnected from server");
                return;
            } catch (JSONException e) {
                System.out.println("Invalid JSON response");
                return;
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("The socket could not be closed");
                    return;
                }
            }
        }
    }

	public void start() {
        /*
        serve images until the server sends stopstream
        */
        // XXX process serversQueue
        Thread receiverThread = new Thread(new ReceiverThread());
        receiverThread.run();
        System.out.println("Hit enter to close the connection.");
        Scanner scanner = new Scanner(System.in);
        while(receiverThread.isAlive()){
            String input = scanner.nextLine();
            if(input.length() == 0)
                receiverThread.interrupt();
        }
	}

    @Override
    public byte[] getImage() {
        return image;
    }

    @Override
    public void setImage(byte[] image) {
        this.image = image;
    }
}
