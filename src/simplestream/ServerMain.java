package simplestream;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class ServerMain {
    public static void main(String[] argv) {
        Params params = new Params();
        try {
            new JCommander(params, argv);
            if (params.rhost == null && params.rport != null) {
                throw new ParameterException("-rport specified but -remote unspecified");
            }
        } catch (ParameterException e) {
            System.out.println(e.toString());
            System.exit(-1);
        }
        boolean isLocal = (params.rhost == null);
        int width = 320, height = 240, fps = 30;
        ImageReceiverInterface receiver;
        if(params.test){
            receiver = new ImageRandomReceiver(width, height, fps);
        }else{
            if(isLocal){
                receiver = new ImageLocalReceiver(width, height, fps);
            }else{
                receiver = new ImageRemoteReceiver(params.sport, params.rhost, params.rport, params.rateLimit, 10000);
            }
        }
        receiver.start();
        new ClientView(width, height, receiver).start();
        new Server(params.sport, params.rhost, params.rport, receiver, params.rateLimit, 3).start();
    }
}
