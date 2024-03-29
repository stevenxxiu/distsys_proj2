package simplestream;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.codec.binary.Base64;
import org.bridj.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerThread implements Runnable {
    Socket clientSocket;
    int rateLimit;
    BufferedReader input;
    BufferedWriter output;
    ImageReceiverInterface receiver;
    Server server;
    // client's server sport
    int csport;

    public ServerThread(Socket clientSocket, int rateLimit, ImageReceiverInterface receiver, Server server) {
        this.clientSocket = clientSocket;
        this.rateLimit = rateLimit;
        this.receiver = receiver;
        this.server = server;
    }

    public int getCsPort(){
        return csport;
    }

    public InetSocketAddress getClientSocketAddress(){
        return new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getPort());
    }

    class ImageSenderThread implements Runnable {
        public void run() {
            JSONObject response;
            try {
                while (true) {
                    System.out.println("Sending image response");
                    response = new JSONObject();
                    try {
                        response.put("response", "image");
                        response.put("data", Base64.encodeBase64String(Compressor.compress(receiver.getImage())));
                    } catch (JSONException e) {
                        assert false;
                    }
                    output.write(response.toString() + "\n");
                    output.flush();
                    try {
                        Thread.sleep(rateLimit);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                // client exited, return to main server thread
                return;
            }
        }
    }

    public void run() {
        JSONObject response;
        // add overloaded response upon first request
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            JSONObject request;
            System.out.println("Receiving startstream request");
            request = new JSONObject(input.readLine());
            if (request.getString("request").equals("startstream")) {
                if (request.has("ratelimit")) {
                    rateLimit = request.getInt("ratelimit");
                }
                if (request.has("sport")) {
                    csport = request.getInt("sport");
                } else {
                    csport = 6262;
                }
            } else {
                System.out.println("Server didn't receive startstream request");
                return;
            }
            if (server.isOverloaded()) {
                System.out.println("Sending overloaded response");
                response = new JSONObject();
                try {
                    response.put("response", "overloaded");
                    if(server.rhost!=null){
                        JSONObject addressResponse = new JSONObject();
                        addressResponse.put("ip", server.rhost);
                        addressResponse.put("port", server.rport);
                        response.put("server", addressResponse);
                    }
                    JSONArray clientAddressesResponse = new JSONArray();
                    for (Pair<Thread, ServerThread> pair : server.serverThreads) {
                        ServerThread serverThread = pair.getSecond();
                        JSONObject addressResponse = new JSONObject();
                        addressResponse.put("ip", serverThread.getClientSocketAddress().getAddress().toString().substring(1));
                        addressResponse.put("port", serverThread.getCsPort());
                        clientAddressesResponse.put(addressResponse);
                    }
                    response.put("clients", clientAddressesResponse);
                } catch (JSONException e) {
                    assert false;
                }
                output.write(response.toString() + "\n");
                output.flush();
                return;
            }
            System.out.println("Sending startingstream response");
            response = new JSONObject();
            try {
                response.put("response", "startingstream");
            } catch (JSONException e) {
                assert false;
            }
            output.write(response.toString() + "\n");
            output.flush();
            // create image-sender thread
            Thread imageSender = new Thread(new ImageSenderThread());
            imageSender.start();
            // read client requests asynchronously
            while (true) {
                System.out.println("Receiving client request");
                request = new JSONObject(input.readLine());
                if (request.getString("request").equals("stopstream")) {
                    System.out.println("Sending stoppedstream response");
                    imageSender.interrupt();
                    response = new JSONObject();
                    try {
                        response.put("response", "stoppedstream");
                    } catch (JSONException e) {
                        assert false;
                    }
                    output.write(response.toString() + "\n");
                    output.flush();
                    System.out.println("Finished sending images");
                    break;
                } else {
                    System.out.println("Unknown request: " + request.getString("request"));
                }
            }
        } catch (JSONException e) {
            System.out.println("Invalid JSON response");
            return;
        } catch (IOException e) {
            System.out.println("Client exited");
            return;
        } finally {
            try {
                System.out.println("Closing client socket");
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("The socket could not be closed");
                return;
            }
        }
    }
}
