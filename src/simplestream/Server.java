package simplestream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws JSONException {

//	boolean started = false;
//	List<Client> clients = new ArrayList<Client>();
//	ServerSocket ss = null;
        try {
            int serverPort = 6262;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            Socket clientSocket = listenSocket.accept();
            System.out.println("Connection Established");
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

//			while (started) {
//				Socket s = ss.accept();
//				Client c = new Client(s);
//				System.out.println("a client connected!");			
//				new Thread(c).start();
//				clients.add(c);			
//			}


            JSONObject status = new JSONObject();
            JSONObject startingstream = new JSONObject();
            JSONObject image = new JSONObject();
            JSONObject stoppedstream = new JSONObject();


            String stream = "local";
            String client = "1";
            String rate = "no";
            String hand = "no";

            try {
                status.put("response", "status");
                status.put("streaming", stream);
                status.put("clients", client);
                status.put("ratelimiting", rate);
                status.put("handover", hand);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            output.writeUTF(status.toString());

            String sReadStartstream = input.readUTF();

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
                System.out.println("server didn't receive startstream request");
            }

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }


    }
}
