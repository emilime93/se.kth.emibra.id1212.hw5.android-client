package hangman.emillb.se.hangmang.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import static hangman.emillb.se.hangmang.activities.HangmanActivity.DEBUG_TAG;

public class ServerHandler {

    private Socket mSocket;
    private BufferedWriter mWriter;
    private BufferedReader mReader;

    private String mHostname;
    private int mPort;
    private boolean mConnected = false;

    private NetworkCallback mCallback;

    public ServerHandler(String host, int port, NetworkCallback callback) {
        this.mHostname = host;
        this.mPort = port;
        this.mCallback = callback;
    }

    public String getHostname() {
        return mHostname;
    }

    public void setHostname(String hostname) {
        mHostname = hostname;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket();
                    mSocket.connect(new InetSocketAddress(mHostname, mPort));
                    mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    new Thread(new ServerListener()).start();
                    mConnected = true;
                    mCallback.notifyConnected();
                    Log.d(DEBUG_TAG, "Connected to " + mSocket);
                } catch (IOException e) {
                    disconnect();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startGame() {
        sendCommand("start");
    }

    public void quitGame() {
        sendCommand("exit");
    }

    public void restartGame() {
        sendCommand("restart");
    }

    public void makeGuess(String message) {
        if (message.length() == 0)
            return;
        if (message.length() == 1)
            sendCommand(message);
        else
            sendCommand("guess " + message);
    }

    private void sendCommand(String message) {
        class MessageSender {
            private String message;

            private MessageSender(String message) {
                this.message = message;
            }

            private void send() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mWriter.write(message + "\n");
                            Log.d(DEBUG_TAG, "Sending:" + message);
                            mWriter.flush();
                            mCallback.messageSent();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        new MessageSender(message).send();
    }

    public void disconnect() {
        try {
            mSocket.close();
            mWriter.close();
            mConnected = false;
            Log.d(DEBUG_TAG, "Disconnected");
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            mCallback.notifyDisconnected();
        }
    }

    private class ServerListener implements Runnable {

        @Override
        public void run() {
            Log.d(DEBUG_TAG, "Server Listener started");
            while(mConnected) {
                try {
                    String received = mReader.readLine();
                    if (received == null) {
                        Log.d(DEBUG_TAG, "NULL RECEIVED, QUITTING");
                        disconnect();
                        return;
                    }
                    mCallback.messageReceived(received);
                    Log.d(DEBUG_TAG,"Server snooped up something");
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnect();
                }
            }
        }
    }
}
