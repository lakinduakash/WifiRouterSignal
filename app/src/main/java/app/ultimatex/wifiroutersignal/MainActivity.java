package app.ultimatex.wifiroutersignal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button startServiceButton;
    Button stopServiceButton;
    Button changeNotificationButton;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startServiceButton =findViewById(R.id.start_service);
        stopServiceButton =findViewById(R.id.stop_service);
        changeNotificationButton=findViewById(R.id.change_notification);

        serviceIntent=new Intent(this,SignalNotifyService.class);

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

        changeNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceIntent.putExtra("CHANGE",1);
                startService(serviceIntent);
            }
        });
    }

    private void startMyService()
    {
        serviceIntent.putExtra("CHANGE",0);
        startService(serviceIntent);
    }

    private void stopMyService()
    {
        stopService(serviceIntent);
    }


}
