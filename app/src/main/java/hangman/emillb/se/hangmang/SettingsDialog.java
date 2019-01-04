package hangman.emillb.se.hangmang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import hangman.emillb.se.hangmang.model.HostTuple;

public class SettingsDialog extends AlertDialog.Builder {

    public interface InputSenderDialogListener{
        void onOK(HostTuple host);
        void onCancel();
    }

    private EditText mHostnameEdit;
    private EditText mPortEdit;

    public SettingsDialog(Activity activity, final InputSenderDialogListener listener) {
        super( new ContextThemeWrapper(activity, R.style.AppTheme) );

        View dialogLayout = LayoutInflater.from(activity).inflate(R.layout.hostname_settings_dialog, null);
        setView(dialogLayout);

        mHostnameEdit = dialogLayout.findViewById(R.id.hostnameEdit);
        mPortEdit = dialogLayout.findViewById(R.id.portEdit);

        setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(listener != null) {
                    String host = String.valueOf(mHostnameEdit.getText());
                    int port = Integer.parseInt(String.valueOf(mPortEdit.getText()));
                    listener.onOK(new HostTuple(host, port));
                }

            }
        });

        setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
    }

    public SettingsDialog setHostname(String hostname){
        mHostnameEdit.setText( hostname );
        return this;
    }

    public SettingsDialog setPort(int number) {
        mPortEdit.setText(String.valueOf(number));
        return this;
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        Window window = dialog.getWindow();
        if( window != null )
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }
}