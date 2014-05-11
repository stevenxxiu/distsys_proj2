package simplestream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread implements Runnable {
    Socket clientSocket;
    DataInputStream input;
    DataOutputStream output;
    Server server;

    public ServerThread(Socket clientSocket, DataInputStream input, DataOutputStream output, Server server) {
        this.clientSocket = clientSocket;
        this.input = input;
        this.output = output;
        this.server = server;
    }

    public void run(){
        JSONObject response = new JSONObject();
        // add overloaded response upon first request
        try{
            String startStreamReqStr = input.readUTF();
            try{
                JSONObject startStreamReq = new JSONObject(startStreamReqStr);
                if (!startStreamReq.getString("request").equals("startstream")) {
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


            JSONObject image = new JSONObject();

            JSONObject JSONReadStartstream = new JSONObject(sReadStartstream);

            System.out.println("status: " + status);
            System.out.println("sReadStartstream:  " + sReadStartstream);
            System.out.println("JSONReadStartstream:  " + JSONReadStartstream);

            if (JSONReadStartstream.getString("request").equals("startstream")) {
                System.out.println("server receive request: startstream");

                try {
                    startingstream.put("response", "startingstream");
                    output.writeUTF(startingstream.toString());
                    Thread sss = new Thread();
                    sss.start();

                    //System.out.println("test");

                    int i = 0;
                    boolean temp = true;
                    //////String sReadStopstream=input.readUTF();

                    while (temp) {

                        image.put("add", i);
                        image.put("response", "image");
                        image.put("data", "IMAGEDATA");
                        i++;
                        output.writeUTF(image.toString());
                        System.out.println("image" + image);

                        if (new JSONObject(input.readUTF()).get("request").equals("stopstream")) {
                            temp = false;
                            System.out.println("server receive stopstream");
                            stoppedstream.put("response", "stoppedstream");
                            output.writeUTF(stoppedstream.toString());
                            System.out.println("server send stoppedstream ");
                            listenSocket.close();
                            clientSocket.close();

                            System.out.println("server TCP close");

                        }//if


                    }//while


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
            }

        } catch (IOException e){
            System.out.println("Client exited");
            return;
        }
    }
}
