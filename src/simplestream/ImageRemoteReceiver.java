package simplestream;

<<<<<<< HEAD
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
=======
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
    String rhost;
    int rateLimit;
    int connectTimeout;
    // list of dead servers, never refreshed
    HashSet<InetSocketAddress> serversDead;
    // list of servers to search for in case the current one is dead
    ConcurrentLinkedQueue<InetSocketAddress> serversQueue;

    public ImageRemoteReceiver(int sport, String rhost, int rport, int rateLimit, int connectTimeout) {
        this.sport = sport;
        this.rport = rport;
        this.rhost = rhost;
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
        private boolean stopStream;

        public ReceiverThread(InetSocketAddress address) {
            this.address = address;
            stopStream = false;
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
                    if (serverStatus.hasHandOver) {
                        if(response.has("clients") || response.has("server")) {
                            System.out.println("Handover data available");
                        }else {
                            System.out.println("Handover implemented on the server but no data available");
                        }
                        // try to connect to another server
                        if(response.has("clients")){
                            System.out.println("Adding server's clients to queue");
                            JSONArray handoverClients = response.getJSONArray("clients");
                            for (int i = 0; i < handoverClients.length(); i++) {
                                JSONObject handoverClient = handoverClients.getJSONObject(i);
                                String rhost = handoverClient.getString("ip");
                                int rport = handoverClient.getInt("port");
                                try {
                                    serversQueue.add(new InetSocketAddress(InetAddress.getByName(rhost), rport));
                                } catch (IOException e) {
                                    System.out.println("Could not find server's address: " + rhost + ":" + rport);
                                }
                            }
                        }
                        if(response.has("server")){
                            System.out.println("Adding server's server to queue");
                            JSONObject handoverServer = response.getJSONObject("server");
                            String rhost = handoverServer.getString("ip");
                            int rport = handoverServer.getInt("port");
                            try {
                                serversQueue.add(new InetSocketAddress(InetAddress.getByName(rhost), rport));
                            } catch (IOException e) {
                                System.out.println("Could not find server's address: " + rhost + ":" + rport);
                            }
                        }
                    }
                    return;
                } else {
                    System.out.println("Invalid response: " + response.getString("response"));
                    return;
                }
                synchronized (connectNotify){
                    System.out.println("Connected to server");
                    connected = true;
                    connectNotify.notifyAll();
                }
                while (true) {
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
                    if(stopStream) {
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
                    System.out.println("Closing client socket");
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("The socket could not be closed");
                    return;
                }
            }
        }

        public void sendStop(){
            // we don't use exceptions here as java streams are un-interruptable
            // won't use NIO as this is takes too much time to do
            stopStream = true;
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
                serversQueue.add(new InetSocketAddress(InetAddress.getByName(rhost), rport));
            } catch (IOException e) {
                System.out.println("Could not find server's address: " + rhost + ":" + rport);
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
                            receiverRunnable.sendStop();
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
                        receiverRunnable.sendStop();
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
>>>>>>> 8776f3f1d988350d2506496f5033ba509abe244e
}
