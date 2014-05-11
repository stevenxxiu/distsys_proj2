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

import org.json.JSONException;
import org.json.JSONObject;

public class ImageReceiver {
	public static void main (String args[]) throws ParseException {    
	    Socket s = null;
	    
		try{
			    
			String hostname="localhost";
		s = new Socket(InetAddress.getByName(hostname),6262);////////////////////////////////////////////????????????????1111111
		System.out.println("Connection Established");
		

	    
		DataInputStream input = new DataInputStream( s.getInputStream());
		DataOutputStream output =new DataOutputStream( s.getOutputStream());
		
		JSONObject startstream= new JSONObject();  
        JSONObject stopstream = new JSONObject(); 
        
        int sport=6262;
        try {
        	startstream.put("request", "startstream");  
        	startstream.put("sport", sport); }

			//obj.put("startstream",startstream);}
		 catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        output.writeUTF(startstream.toString()) ;                    
        // out.write(obj.toString().getBytes());  
        String cReadStatus=input.readUTF();
        System.out.println("cReadStatus:  "+cReadStatus);
        
        String cReadStartingStream=input.readUTF();
        System.out.println("cReadStartingStream:  "+cReadStartingStream);
        
        
        
        
        
		try {
			Thread.sleep(10);
			System.out.println("client sleep 1000");
			
			try {
				stopstream.put("request", "stopstream");
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			output.writeUTF(stopstream.toString());
			System.out.println("send request stopstream");
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//String cReadStoppedstream=input.readUTF();
		List<String> readList = new ArrayList<String>();
		while(true){
			readList.add(input.readUTF());
		
		
		 if (readList.get(readList.size()).contains("stoppedstream")){
		System.out.println("cReadStoppedstream:  "+input.readUTF());
		s.close();
		System.out.println("client TCP close");
		 }break;}
		 
		 
		 
		 
		 
}catch (UnknownHostException e) {
	System.out.println("Socket:"+e.getMessage());
}catch (EOFException e){
System.out.println("EOF:"+e.getMessage());
}catch (IOException e){
System.out.println("readline:"+e.getMessage());
}finally {
if(s!=null) try {
s.close();
}catch (IOException e){
System.out.println("close:"+e.getMessage());
      }
		}}}
