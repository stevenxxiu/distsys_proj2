package simplestream;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread implements Runnable {
    int rateLimit;
    Socket clientSocket;
    DataInputStream input;
    DataOutputStream output;
    ImageReceiverInterface receiver;
    Server server;

    public ServerThread(Socket clientSocket, DataInputStream input, DataOutputStream output, int rateLimit,
                        ImageReceiverInterface receiver, Server server) {
        this.clientSocket = clientSocket;
        this.input = input;
        this.output = output;
        this.rateLimit = rateLimit;
        this.receiver = receiver;
        this.server = server;
    }

    class ImageSenderThread implements Runnable {
        public void run() {
            try {
                while (true) {
                    // send image to client
                    output.writeUTF(Base64.encodeBase64String(Compressor.compress(receiver.getImage())));
                    output.flush();
                    try {
                        Thread.sleep(rateLimit);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Client exited");
                return;
            }
        }
    }

    public void run() {
        JSONObject response;
        // add overloaded response upon first request
        try {
            JSONObject request;
            String requestStr = input.readUTF();
            request = new JSONObject(requestStr);
            if (request.getString("request").equals("startstream")) {
                if (request.has("ratelimit")) {
                    rateLimit = request.getInt("ratelimit");
                }
            } else {
                System.out.println("Server didn't receive startstream request");
                return;
            }
            if (server.isOverloaded()) {
                response = new JSONObject();
                try {
                    response.put("response", "overloaded");
                } catch (JSONException e) {
                    assert false;
                }
                output.writeUTF(response.toString());
                output.flush();
                clientSocket.close();
                return;
            }
            response = new JSONObject();
            try {
                response.put("response", "startingstream");
            } catch (JSONException e) {
                assert false;
            }
            output.writeUTF(response.toString());
            output.flush();
            // create image-sender thread
            Thread imageSender = new Thread(new ImageSenderThread());
            imageSender.run();
            // read client requests asynchronously
            while (true) {
                request = new JSONObject(requestStr);
                if (request.getString("request").equals("stopstream")) {
                    imageSender.interrupt();
                    response = new JSONObject();
                    try {
                        response.put("response", "stoppedstream");
                    } catch (JSONException e) {
                        assert false;
                    }
                    break;
                } else {
                    System.out.println("Unknown request: " + request.getString("request"));
                }
            }
            clientSocket.close();
        } catch (JSONException e) {
            System.out.println("Invalid JSON response");
            return;
        } catch (IOException e) {
            System.out.println("Client exited");
            return;
        }
    }
}
