package simplestream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class ImageRemoteReceiver implements ImageReceiverInterface{
    public byte[] image;

    public void start(){
        Socket SocketImageReceiver = null;
        try {
            String hostname = "localhost";
            int port = 6262;
            SocketImageReceiver = new Socket(InetAddress.getByName(hostname),
                    port);
            DataInputStream input = new DataInputStream(SocketImageReceiver
                    .getInputStream());
            DataOutputStream output = new DataOutputStream(SocketImageReceiver
                    .getOutputStream());

            JSONObject startstream = new JSONObject();
            JSONObject stopstream = new JSONObject();
            try {
                startstream.put("request", "startstream");
                startstream.put("sport", "port");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            output.writeUTF(startstream.toString());
            JSONObject JSONReadStartingStream = null;
            try {
                JSONReadStartingStream = new JSONObject(input.readUTF());
            } catch (JSONException e2) {
                System.out.println("Invalid JSON response");
                return;
            }
            try {
                if (JSONReadStartingStream.get("reponse").equals("overloaded")) {
                    System.out.println("Server Overloaded, TCP Close");
                    SocketImageReceiver.close();
                }
            } catch (JSONException e1) {
                System.out.println("Invalid JSON response");
                return;
            }

            Scanner keyboard = new Scanner(System.in);
            System.out.println("Input stop to close the connection");
            String order = keyboard.next();
            if (order.equals("stop")) {
                try {
                    stopstream.put("request", "stopstream");
                } catch (JSONException e) {
                    System.out.println("Invalid JSON response");
                    return;
                }
                output.writeUTF(stopstream.toString());
                System.out.println("stop and send request stopstream");
            }

            while (true) {
                readImageList.add(input.readUTF());

                if (readImageList.get(readImageList.size()).contains(
                        "stoppedstream")) {
                    System.out.println("cReadStoppedstream:  "
                            + input.readUTF());
                    SocketImageReceiver.close();
                    System.out.println("client TCP close");
                }
                break;
            }

        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        } finally {
            if (SocketImageReceiver != null)
                try {
                    SocketImageReceiver.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
        }
    }
};
}