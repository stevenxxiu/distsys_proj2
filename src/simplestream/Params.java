package simplestream;

import com.beust.jcommander.Parameter;

public class Params {
    @Parameter(names = "-sport", required = false, description = "Server port")
    public Integer sport = 6262;

    @Parameter(names = "-remote", required = false, description = "Remote url")
    public String remoteUrl = null;

    @Parameter(names = "-rport", required = false, description = "Remote port")
    public Integer rport = null;

    @Parameter(names = "-rate", required = false, description = "Sleep time (ms)")
    public Integer rate = 100;
}
