package simplestream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageRemoteReceiver implements ImageReceiverInterface {
    byte[] image;
    int sport;
	int port;
	String hostname;

	public ImageRemoteReceiver(int sport, int port, String hostname) {
        this.sport = sport;
		this.port = port;
		this.hostname = hostname;
	}

	public void start() {
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
            // send startstream request
            request = new JSONObject();
            try{
                request.put("request", "startstream");
                request.put("sport", sport);
            }catch(JSONException e){
                assert false;
            }
            output.writeUTF(request.toString());
            output.flush();
            // read server's response
            try {
                response = new JSONObject(input.readUTF());
                if(response.getString("response").equals("startingstream")) {
                }else if(response.getString("response").equals("overloaded")){
                    System.out.println("Server overloaded");
                    return;
                }else{
                    System.out.println("Invalid response: " + response.getString("response"));
                    return;
                }
            } catch (JSONException e) {
                System.out.println("Invalid JSON response");
                return;
            }
            // XXX interrupt the server reads immediately using the console
            while(true){
                try {
                    response = new JSONObject(input.readUTF());
                    if(response.getString("response").equals("image")) {
                        image = Compressor.decompress(Base64.decode(response.getString("data")));
                    }else{
                        System.out.println("Client didn't receive an image response");
                        return;
                    }
                } catch (JSONException e) {
                    System.out.println("Invalid JSON response");
                    return;
                }
            }
		} catch(IOException e){
            System.out.println("Disconnected from server");
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

    @Override
    public byte[] getImage() {
        return image;
    }

    @Override
    public void setImage(byte[] image) {
        this.image = image;
    }
}
