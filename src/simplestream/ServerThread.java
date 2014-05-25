package simplestream;

<<<<<<< HEAD
import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import javax.swing.JFrame;

import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;

public class ServerThread implements Runnable {
	int rateLimit;
	Socket clientSocket;
	DataInputStream input;
	DataOutputStream output;
	Server server;

	public ServerThread(Socket clientSocket, DataInputStream input,
			DataOutputStream output, int rateLimit, Server server) {
		this.clientSocket = clientSocket;
		this.input = input;
		this.output = output;
		this.rateLimit = rateLimit;
		this.server = server;
	}

	public void run() {
		JSONObject response;
		JSONObject jsonObj;
		// add overloaded response upon first request
		System.out.println("serverthread start");
		try {
			String requestStr = input.readUTF();
			System.out.println("requestStr " + requestStr);
			try {
				JSONObject request = new JSONObject(requestStr);

				if (!request.getString("request").equals("startstream")) {
					System.out
							.println("Server didn't receive startstream request");
					return;
				}
			} catch (JSONException e) {
				System.out.println("Invalid JSON response");
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
			System.out.println("response " + response);
			output.writeUTF(response.toString());
			output.flush();
			// send images to client
			while (true) {
				try {
					JSONObject request = new JSONObject(requestStr);
					if (request.getString("request").equals("stopstream")) {
						response = new JSONObject();
						try {
							response.put("response", "stoppedstream");
							break;
						} catch (JSONException e) {
							assert false;
						}
						break;
					} else if (request.getString("request").equals("ratelimit")) {
						rateLimit = request.getInt("ratelimit");
					} else {
						
/*****************************************************************************************************************************/

						
						Viewer myViewer = new Viewer();
						JFrame frame = new JFrame("Simple Stream Viewer");
						frame.setVisible(true);
						frame.setSize(320, 240);
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						
						frame.add(myViewer);
						/**
						 * This example show how to use native OpenIMAJ API to capture raw bytes
						 * data as byte[] array. It also calculates current FPS.
						 */

						OpenIMAJGrabber grabber = new OpenIMAJGrabber();

						Device device = null;
						Pointer<DeviceList> devices = grabber.getVideoDevices();
						for (Device d : devices.get().asArrayList()) {
							device = d;
							break;
						}

						boolean started = grabber.startSession(320, 240, 30, Pointer.pointerTo(device));
						if (!started) {
							throw new RuntimeException("Not able to start native grabber!");
						}

						int n = 1000;
						int i = 0;
						do {
							/* Get a frame from the webcam. */
							grabber.nextFrame();
							/* Get the raw bytes of the frame. */
							byte[] raw_image=grabber.getImage().getBytes(320 * 240 * 3);
							/* Apply a crude kind of image compression. */
							byte[] compressed_image = Compressor.compress(raw_image);
							/* Prepare the date to be sent in a text friendly format. */
							byte[] base64_image = Base64.encodeBase64(compressed_image);
							/*
							 * The image data can be sent to connected clients.
							 */
							/*
							 * Assume we received some image data.
							 * Remove the text friendly encoding.
							 */
							
							
							response = new JSONObject();
							jsonObj = new JSONObject();
							jsonObj.put("response", "image");
							jsonObj.put("data",base64_image.toString());
							System.out.println(jsonObj);
							output.writeUTF(jsonObj.toString());
						} while (++i < n);
						
						grabber.stopSession();
					/**********************************************************************************************/
						try {
							Thread.sleep(rateLimit);
						} catch (InterruptedException e) {
							break;
						}
					}
				} catch (JSONException e) {
					System.out.println("Invalid JSON response");
					return;
				}
				// XXX send image to client

				
			}
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("Client exited");
			return;
		}
	}
=======
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
>>>>>>> 8776f3f1d988350d2506496f5033ba509abe244e
}
