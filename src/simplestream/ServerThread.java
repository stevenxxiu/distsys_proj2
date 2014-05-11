package simplestream;

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
    Server server;

    public ServerThread(Socket clientSocket, DataInputStream input, DataOutputStream output, int rateLimit, Server server) {
        this.clientSocket = clientSocket;
        this.input = input;
        this.output = output;
        this.rateLimit = rateLimit;
        this.server = server;
    }

    public void run(){
        JSONObject response;
        // add overloaded response upon first request
        try{
            String requestStr = input.readUTF();
            try{
                JSONObject request = new JSONObject(requestStr);
                if (!request.getString("request").equals("startstream")) {
                    System.out.println("Server didn't receive startstream request");
                    return;
                }
            }catch(JSONException e){
                System.out.println("Invalid JSON response");
                return;
            }
            if(server.isOverloaded()){
                response = new JSONObject();
                try{
                    response.put("response", "overloaded");
                }catch(JSONException e){
                    assert false;
                }
                output.writeUTF(response.toString());
                output.flush();
                clientSocket.close();
                return;
            }
            response = new JSONObject();
            try{
                response.put("response", "startingstream");
            }catch(JSONException e){
                assert false;
            }
            output.writeUTF(response.toString());
            output.flush();
            // send images to client
            while(true){
                try{
                    JSONObject request = new JSONObject(requestStr);
                    if (request.getString("request").equals("stopstream")) {
                        break;
                    }else if(request.getString("request").equals("ratelimit")){
                        rateLimit = request.getInt("ratelimit");
                    }else{
                        System.out.println("Unknown request: " + request.getString("request"));
                    }
                }catch(JSONException e){
                    System.out.println("Invalid JSON response");
                    return;
                }
                // XXX send image to client

                try{
                    Thread.sleep(rateLimit);
                }catch(InterruptedException e){
                    break;
                }
            }
            clientSocket.close();
        } catch (IOException e){
            System.out.println("Client exited");
            return;
        }
    }
}
