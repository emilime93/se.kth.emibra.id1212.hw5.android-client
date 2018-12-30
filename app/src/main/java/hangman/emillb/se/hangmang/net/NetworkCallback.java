package hangman.emillb.se.hangmang.net;

import android.net.NetworkInfo;

public interface NetworkCallback {
    void messageSent();
    void messageReceived(String message);
    void notifyConnected();
    void notifyDisconnected();
}
