package simplestream;

import com.beust.jcommander.Parameter;

public class Params {
    @Parameter(names = "-sport", required = false, description = "Server port")
    public Integer sport = 6262;

    @Parameter(names = "-remote", required = false, description = "Remote url")
<<<<<<< HEAD
    public String remoteUrl = null;
=======
    public String rhost = null;
>>>>>>> 8776f3f1d988350d2506496f5033ba509abe244e

    @Parameter(names = "-rport", required = false, description = "Remote port")
    public Integer rport = null;

    @Parameter(names = "-rate", required = false, description = "Sleep time (ms)")
    public Integer rateLimit = 100;
<<<<<<< HEAD
=======

    @Parameter(names = "-test", required = false, description = "Use random images for testing")
    public boolean test = false;
>>>>>>> 8776f3f1d988350d2506496f5033ba509abe244e
}
