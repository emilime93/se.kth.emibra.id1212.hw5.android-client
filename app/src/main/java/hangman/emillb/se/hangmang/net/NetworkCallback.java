package hangman.emillb.se.hangmang.net;

public interface NetworkCallback {
    void messageSent();
    void messageReceived(String message);
    void notifyConnected();
    void notifyDisconnected();
}
