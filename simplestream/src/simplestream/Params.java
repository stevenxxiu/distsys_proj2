package simplestream;

import com.beust.jcommander.Parameter;

public class Params {
    @Parameter(names = "-sport", required = false, description = "Server port")
    public Integer sport = 6262;

    @Parameter(names = "-remote", required = false, description = "Remote url")
    public String rhost = null;

    @Parameter(names = "-rport", required = false, description = "Remote port")
    public Integer rport = null;

    @Parameter(names = "-rate", required = false, description = "Sleep time (ms)")
    public Integer rateLimit = 100;

    @Parameter(names = "-test", required = false, description = "Use random images for testing")
    public boolean test = false;
}
