package simplestream;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageRemoteReceiver implements ImageReceiverInterface {
    byte[] image;
    final Object imageNotify;
    int sport;
    int rport;
    String hostname;
    int rateLimit;
    int connectTimeout;
    // list of dead servers, never refreshed
    HashSet<InetSocketAddress> serversDead;
    // list of servers to search for in case the current one is dead
    ConcurrentLinkedQueue<InetSocketAddress> serversQueue;

    public ImageRemoteReceiver(int sport, int rport, String hostname, int rateLimit, int connectTimeout) {
        this.sport = sport;
        this.rport = rport;
        this.hostname = hostname;
        this.rateLimit = rateLimit;
        this.connectTimeout = connectTimeout;
        imageNotify = new Object();
        serversDead = new HashSet<InetSocketAddress>();
        serversQueue = new ConcurrentLinkedQueue<InetSocketAddress>();
    }

    class ReceiverThread implements Runnable {
        InetSocketAddress address;
        // notifies when the client has connected or stopped connecting with the server (server starts sending images)
        // use a separate notifier per-thread to allow for concurrent test-connections
        boolean connected = false;
        final Object connectNotify;

        private BufferedReader input;
        private BufferedWriter output;
        private boolean streamsInterrupted = false;

        public ReceiverThread(InetSocketAddress address) {
            this.address = address;
            connectNotify = new Object();
        }

        public void connect() {
            /*
            receives images until the server sends stopstream
            */
            Socket clientSocket;
            try {
                clientSocket = new Socket(address.getAddress(), address.getPort());
            } catch (IOException e) {
                System.out.println("Could not connect to server: " + address.getAddress() + ":" + address.getPort());
                return;
            }
            JSONObject request, response;
            try {
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                try {
                    ServerStatus serverStatus;
                    System.out.println("Receiving status response");
                    response = new JSONObject(input.readLine());
                    if (response.getString("response").equals("status")) {
                        serverStatus = ServerStatus.fromJSON(response);
                    } else {
                        System.out.println("Invalid response: " + response.getString("response"));
                        return;
                    }
                    System.out.println("Sending startstream request");
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
                    output.write(request.toString() + "\n");
                    output.flush();
                    System.out.println("Receiving server response");
                    response = new JSONObject(input.readLine());
                    if (response.getString("response").equals("startingstream")) {
                    } else if (response.getString("response").equals("overloaded")) {
                        System.out.println("Server overloaded");
                        if (serverStatus.hasHandOver && !serverStatus.isLocal) {
                            // try to connect to another server
                            InetSocketAddress address;
                            JSONArray handoverClients = response.getJSONArray("clients");
                            JSONObject handoverServer = response.getJSONObject("clients");
                            System.out.println("Handover data is present, adding to search queue");
                            for (int i = 0; i < handoverClients.length(); i++) {
                                JSONObject handoverClient = handoverClients.getJSONObject(i);
                                address = new InetSocketAddress(handoverClient.getString("ip"), handoverClient.getInt("port"));
                                if (!serversDead.contains(address))
                                    serversQueue.add(address);
                            }
                            address = new InetSocketAddress(handoverServer.getString("ip"), handoverServer.getInt("port"));
                            if (!serversDead.contains(address))
                                serversQueue.add(address);
                        }
                        return;
                    } else {
                        System.out.println("Invalid response: " + response.getString("response"));
                        return;
                    }
                } catch (IOException e) {
                    if(!streamsInterrupted)
                        throw e;
                    System.out.println("Handshake not finished yet, force disconnect from server");
                    return;
                }
                synchronized (connectNotify){
                    System.out.println("Connected to server");
                    connected = true;
                    connectNotify.notifyAll();
                }
                while (true) {
                    try {
                        System.out.println("Receiving image response");
                        response = new JSONObject(input.readLine());
                        if (response.getString("response").equals("image")) {
                            synchronized (imageNotify){
                                image = Compressor.decompress(Base64.decodeBase64(response.getString("data")));
                                imageNotify.notifyAll();
                            }
                        } else {
                            System.out.println("Client didn't receive an image");
                            return;
                        }
                    } catch (IOException e) {
                        if(!streamsInterrupted)
                            throw e;
                        input.reset();
                        System.out.println("Sending stopstream request");
                        request = new JSONObject();
                        try {
                            request.put("request", "stopstream");
                            request.put("sport", sport);
                        } catch (JSONException e_) {
                            assert false;
                        }
                        output.write(request.toString() + "\n");
                        output.flush();
                        System.out.println("Receiving stopstream response");
                        response = new JSONObject(input.readLine());
                        if (response.getString("response").equals("stoppedstream")) {
                        } else {
                            System.out.println("Invalid response: " + response.getString("response"));
                            return;
                        }
                        System.out.println("Finished receiving images");
                        return;
                    }
                }
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

        public void interruptStreams(){
            try {
                input.close();
                output.close();
                streamsInterrupted = true;
            } catch(IOException e){}
        }

        public void run() {
            connect();
            synchronized (connectNotify){
                connected = false;
                connectNotify.notifyAll();
            }
        }
    }

    class ReplThread implements Runnable {
        public void run(){
            /*
            receives images until the server or user exits
            */
            try {
                serversQueue.add(new InetSocketAddress(InetAddress.getByName(hostname), rport));
            } catch (IOException e) {
                System.out.println("Could not find server's address: " + hostname + ":" + rport);
                return;
            }
            // search for a working server, one at a time, using BFS
            ReceiverThread receiverRunnable = null;
            Thread receiverThread = null;
            try {
                while (!serversQueue.isEmpty()) {
                    System.out.println("Trying next server in queue");
                    InetSocketAddress address = serversQueue.remove();
                    receiverRunnable = new ReceiverThread(address);
                    receiverThread = new Thread(receiverRunnable);
                    receiverThread.start();
                    // wait for server's success or failure response
                    synchronized (receiverRunnable.connectNotify){
                        receiverRunnable.connectNotify.wait(connectTimeout);
                        if (receiverRunnable.connected) {
                            break;
                        } else {
                            receiverRunnable.interruptStreams();
                            receiverThread.interrupt();
                            serversDead.add(address);
                        }
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
            // start an interactive prompt
            if (receiverRunnable != null && receiverRunnable.connected) {
                System.out.println("Hit enter to close the connection.");
                Scanner scanner = new Scanner(System.in);
                while (receiverThread.isAlive()) {
                    String input = scanner.nextLine();
                    if (input.length() == 0) {
                        receiverRunnable.interruptStreams();
                        receiverThread.interrupt();
                    }
                }
            }
        }
    }

    public void start() {
        new Thread(new ReplThread()).start();
    }

    @Override
    public byte[] getImage() {
        synchronized (imageNotify) {
            return image.clone();
        }
    }

    @Override
    public byte[] getNextImage() {
        synchronized (imageNotify){
            try {
                imageNotify.wait();
            }catch(InterruptedException e){}
            return image.clone();
        }
    }
}
