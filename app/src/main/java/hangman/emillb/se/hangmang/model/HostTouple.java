package hangman.emillb.se.hangmang.model;

public class HostTouple {
    private String mHostname;
    private int mPort;

    public HostTouple(String hostname, int port) {
        mHostname = hostname;
        mPort = port;
    }

    public String getHostname() {
        return mHostname;
    }

    public int getPort() {
        return mPort;
    }
}
