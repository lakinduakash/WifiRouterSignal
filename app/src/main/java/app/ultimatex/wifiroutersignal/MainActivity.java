package app.ultimatex.wifiroutersignal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import app.ultimatex.wifiroutersignal.tiny.TinyDB;

public class MainActivity extends AppCompatActivity {

    private String addr;
    public static final String WIFI_ADDRESS = "wifi_address";

    private Button startServiceButton;
    private Button stopServiceButton;
    private Intent serviceIntent;
    private EditText editText;
    private Button setAddress;
    private TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startServiceButton = findViewById(R.id.start_service);
        stopServiceButton = findViewById(R.id.stop_service);
        editText = findViewById(R.id.set_address_editText);
        setAddress = findViewById(R.id.set_address_button);

        tinyDB = new TinyDB(this);
        addr = tinyDB.getString(WIFI_ADDRESS);

        setButtonListeners();
        textFieldInit();


    }

    private void startMyService() {
        serviceIntent.putExtra(WIFI_ADDRESS, addr);
        startService(serviceIntent);
    }

    private void stopMyService() {
        stopService(serviceIntent);
    }

    private void putAddr() {
        tinyDB.putString(WIFI_ADDRESS, editText.getText().toString());
        addr = tinyDB.getString(WIFI_ADDRESS);
    }

    private AlertDialog buildDialog(DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        return builder.setTitle(R.string.start_service_dialog_title)
                .setMessage(R.string.start_service_dialog_message)
                .setCancelable(true)
                .setPositiveButton(R.string.No, positiveListener)
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private void setButtonListeners() {
        serviceIntent = new Intent(this, SignalNotifyService.class);

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMyService();
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMyService();
            }
        });


        setAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String a = editText.getText().toString().trim();

                if (a != null && addr != null && !a.equals(addr)) {
                    buildDialog(new DialogInterface.OnClickListener() {


                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopMyService();
                            putAddr();
                            startMyService();
                            editText.setInputType(InputType.TYPE_NULL);
                        }
                    }).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_address_is_already_set, Toast.LENGTH_SHORT).show();
                    editText.setInputType(InputType.TYPE_NULL);
                }

            }
        });
    }

    private void textFieldInit() {
        if (addr == null || addr == "") {
            addr = Connection.DEFAULT_ADDR;
        }

        editText.setText(addr);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
            }
        });


        editText.setInputType(InputType.TYPE_NULL);
    }


}
