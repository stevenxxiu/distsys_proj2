package simplestream;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Server {
    public static void main(String[] argv) {
        Params params = new Params();
        try{
            new JCommander(params, argv);
            if(params.remoteUrl==null && params.rport!=null){
                throw new ParameterException("-rport specified but -remoteUrl unspecified");
            }
        }catch(ParameterException e){
            System.out.println(e);
            System.exit(-1);
        }
        boolean isLocal = (params.remoteUrl==null);

    }
}
