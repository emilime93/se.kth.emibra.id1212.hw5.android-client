package hangman.emillb.se.hangmang;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import hangman.emillb.se.hangmang.model.GameActionFeedback;
import hangman.emillb.se.hangmang.model.HostTuple;
import hangman.emillb.se.hangmang.net.NetworkCallback;
import hangman.emillb.se.hangmang.net.ServerHandler;

public class HangmanActivity extends AppCompatActivity implements NetworkCallback {

    private final static String TAG = ServerHandler.class.getSimpleName();

    private Button mGuessButton;
    private Button mStartGameButton;
    private Button mQuitButton;

    private TextView mProgressView;
    private TextView mRemainingGuessesView;
    private TextView mScoreView;
    private TextView mSecretWordView;
    private TextView mGuessedView;

    private EditText mGuessInput;

    private boolean mGameOngoing = false;

    private boolean mConnectedToServer = false;

    private ServerHandler mServerHandler = new ServerHandler("192.168.0.5", 4455, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hangman_activity_main);

        mGuessButton = findViewById(R.id.guessButton);
        mProgressView = findViewById(R.id.progressView);
        mGuessInput = findViewById(R.id.guessInput);
        mRemainingGuessesView = findViewById(R.id.remainingGuessesView);
        mScoreView = findViewById(R.id.scoreView);
        mSecretWordView = findViewById(R.id.secretWordView);
        mStartGameButton = findViewById(R.id.startGameButton);
        mQuitButton = findViewById(R.id.quitButton);
        mGuessedView = findViewById(R.id.guessedView);

        setGameInteractionEnabled(false);
        mServerHandler.connect();

        mGuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServerHandler.makeGuess(mGuessInput.getText().toString());
            }
        });

        mStartGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSecretWordView.setText(null);
                if (mGameOngoing) {
                    Log.d(TAG, "Trying to restart game");
                    mServerHandler.restartGame();
                } else {
                    Log.d(TAG, "Trying to START game");
                    mServerHandler.startGame();
                }
            }
        });

        mQuitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectedToServer)
                    mServerHandler.quitGame();
                else
                    mServerHandler.connect();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_action:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        new SettingsDialog(HangmanActivity.this, new SettingsDialog.InputSenderDialogListener() {
            @Override
            public void onOK(final HostTuple host) {
                mServerHandler.setHostname(host.getHostname());
                mServerHandler.setPort(host.getPort());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Dialog canceled");
            }
        }).setHostname(mServerHandler.getHostname()).setPort(mServerHandler.getPort()).show();
    }

    private void clearUI(final boolean fullClear) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fullClear) {
                    mRemainingGuessesView.setText(R.string.remaining_guess_prefix);
                    mScoreView.setText(R.string.score_prefix);
                    mProgressView.setText(null);
                }
                mSecretWordView.setText(null);
                mGuessedView.setText(null);
            }
        });
    }

    @Override
    public void messageSent() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGuessInput.setText(null);
            }
        });
    }

    /*
     * FORMAT FOR THE INFORMATION SENDING:
     * GAME_STATE|W _ R _|A,B,C,D|(rem_attempts:int)|(score:int)[|secretWord:String]
     *           CORRECT  GUESSED
     * */
    @Override
    public void messageReceived(String message) {
        if (!message.contains("|"))
            informAction(message);
        else {
            class UIRunner {
                String[] params;

                private UIRunner(String[] params) {
                    this.params = params;
                }

                void updateUI() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressView.setText(params[1].trim());
                            if (!params[2].trim().equals("_")) {
                                mGuessedView.setText(getString(R.string.guessed_prefix) + " " + params[2]);
                            }
                            mRemainingGuessesView.setText(getString(R.string.remaining_guess_prefix) + " " + params[3]);
                            mScoreView.setText(getString(R.string.score_prefix) + " " + params[4]);
                            if (params.length > 5) {
                                mSecretWordView.setText(getString(R.string.secret_prefix) + " " + params[5]);
                            }
                        }
                    });
                }
            }
            String[] params = message.split("\\|");
            new UIRunner(params).updateUI();
        }
    }

    @Override
    public void notifyConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectedToServer = true;
                mQuitButton.setText(R.string.quit_game_button);
                setGameInteractionEnabled(true);
                Toast.makeText(HangmanActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGameInteractionEnabled(boolean enabled) {
        mGuessButton.setEnabled(enabled);
        mStartGameButton.setEnabled(enabled);
    }

    @Override
    public void notifyDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectedToServer = false;
                setGameInteractionEnabled(false);
                mQuitButton.setText(R.string.reconnect_game_button);
                Toast.makeText(HangmanActivity.this, "Disconnected from server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGameOngoing(boolean ongoing) {
        mGameOngoing = ongoing;
        if (mGameOngoing) {
            mStartGameButton.setText(R.string.restart_game_button);
        } else {
            mStartGameButton.setText(R.string.start_game_button);
        }
    }

    private void informAction(String message) {
        class UIRunner {
            private String message;

            private UIRunner(String message) {
                this.message = message;
            }

            private void displayMessage() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String toastMsg = "Unknown message";
                        GameActionFeedback gaf = GameActionFeedback.valueOf(message);
                        switch (gaf) {
                            case DUPLICATE_GUESS:
                                toastMsg = "You already guessed this";
                                break;
                            case NO_GAME_STARTED:
                                toastMsg = "No game started";
                                break;
                            case GAME_WON:
                                setGameOngoing(false);
                                toastMsg = "YOU WON!";
                                break;
                            case GAME_LOST:
                                setGameOngoing(false);
                                toastMsg = "YOU LOST";
                                break;
                            case GAME_QUIT:
                                toastMsg = "Quit game";
                                clearUI(true);
                                break;
                            case GAME_ONGOING:
                                toastMsg = "A game is already ongoing";
                                break;
                            case GAME_STARTED:
                                setGameOngoing(false);
                                toastMsg = "Game started";
                                clearUI(false);
                                setGameOngoing(true);
                                break;
                            case GAME_RESTARTED:
                                toastMsg = "Game restarted";
                                clearUI(false);
                                setGameOngoing(true);
                                break;
                            case INVALID_COMMAND:
                                toastMsg = "Invalid command";
                                break;
                            case HELP:
                            case GAME_INFO:
                                return;
                        }
                        Toast.makeText(HangmanActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        new UIRunner(message).displayMessage();
    }
}
