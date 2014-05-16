package simplestream;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;

public class ImageRemoteReceiver implements ImageReceiverInterface {
	
	int port;
	String hostname;

	public ImageRemoteReceiver(int port, String hostname) {
		this.port = port;
		this.hostname = hostname;
	}

	public void start() {
		Socket SocketImageReceiver = null;

		try {
			SocketImageReceiver = new Socket(InetAddress.getByName(hostname),
					port);
			DataInputStream input = new DataInputStream(SocketImageReceiver
					.getInputStream());
			DataOutputStream output = new DataOutputStream(SocketImageReceiver
					.getOutputStream());
			JSONObject status = new JSONObject();
			JSONObject startstream = new JSONObject();
			JSONObject stopstream = new JSONObject();
			JSONObject JSONReadImageStream = new JSONObject();

			try {
				// if (Integer.valueOf(port) == null) {
				// port = 6262;
				// }
				status = new JSONObject(input.readUTF());
				System.out.println(status);
				startstream.put("request", "startstream");
				startstream.put("sport", port);

			} catch (JSONException e) {
				System.out.println("Invalid JSON response1");
				return;
			}
			output.writeUTF(startstream.toString());
			JSONObject JSONReadStartingStream = null;

			try {
				JSONReadStartingStream = new JSONObject(input.readUTF());
				System.out.println(JSONReadStartingStream);
			} catch (JSONException e2) {
				System.out.println("Invalid JSON response2");
				return;
			}

			try {

				if (JSONReadStartingStream.getString("response").equals(
						"overloaded")) {
					System.out.println("Server Overloaded, TCP Close");
					SocketImageReceiver.close();
				}
			} catch (JSONException e1) {
				System.out.println("Invalid JSON response3");
				return;
			}

			while (true) {
				try {
					JSONReadImageStream = new JSONObject(input.readUTF());
				} catch (JSONException e2) {
					System.out.println("Invalid JSON response5");
					return;
				}

				try {
					if (JSONReadImageStream.getString("response").equals(
							"image")) {
						
/*****************************************************************************************************************************/

						
						
					/**********************************************************************************************/
						
						byte[] b = input.readUTF().getBytes("UTF-8");
						System.out.println(b);
						
						Viewer myViewer = new Viewer();
						JFrame frame = new JFrame("Simple Stream Viewer");
						frame.setSize(320, 240);
						frame.setVisible(true);
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						
						frame.add(myViewer);
						//myViewer.ViewerInput(b);
						frame.repaint();
						byte[] c = Base64.decodeBase64(b);
						byte[] image = Compressor.decompress(c);
						BufferedImage img = ImageIO
								.read(new ByteArrayInputStream(image));
						System.out.println(img);
						// image = viewer.ViewerInput(image2);
						// g.drawImage(image, 0, 0, null);
					} else {
						SocketImageReceiver.close();
						System.out.println("client TCP close");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}

			Scanner keyboard = new Scanner(System.in);
			System.out.println("Input 'Enter' to close the connection");

			String order = keyboard.next();
			System.out.println(order.length());

			if (order.length() == 0) {
				try {
					System.out.println("Client Stop");
					stopstream.put("request", "stopstream");
				} catch (JSONException e) {
					System.out.println("Invalid JSON response4");
					return;
				}
				output.writeUTF(stopstream.toString());
				System.out.println("stop and send request stopstream");
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

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		Socket s = null;
		ImageRemoteReceiver client = new ImageRemoteReceiver(6262, "localhost");
		// s = new Socket("localhost",6262);
		client.start();

	}
}
