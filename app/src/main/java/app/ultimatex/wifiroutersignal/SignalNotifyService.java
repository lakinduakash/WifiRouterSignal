package app.ultimatex.wifiroutersignal;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import app.ultimatex.wifiroutersignal.tiny.TinyDB;

public class SignalNotifyService extends Service {

    public static final int NOTIFICATION_ID = 5;
    public static final int NOTIFICATION_ID_NEW_USER = 4;
    public static final String NOTIFICATION_CHANNEL_NEW_USER = "MY_CHANNEL_NEW_USER";
    public static final String NOTIFICATION_CHANNEL = "MY_CHANNEL";

    private String addr;
    private TinyDB tinyDB;
    volatile private int prevUserCount = 1;
    volatile private int curUserCount = 0;


    private NotificationCompat.Builder builder;
    private NotificationUpdater u;

    private boolean stopTask = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        tinyDB = new TinyDB(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addr = intent.getStringExtra(MainActivity.WIFI_ADDRESS);
        if (addr == null)
            addr = tinyDB.getString(MainActivity.WIFI_ADDRESS);
        if ("".equals(addr))
            addr = "http://homerouter.cpe";

        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentText("Started")
                .setSmallIcon(R.drawable.ic_network_check_white_24dp)
                .setContentTitle("Connected")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        runTask();

        return super.onStartCommand(intent, flags, startId);

    }


    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }



    @Override
    public void onDestroy() {
        stopTask = true;
        super.onDestroy();
    }

    private void runTask() {
        u = new NotificationUpdater();
        u.execute();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    class NotificationUpdater extends AsyncTask<Void, Void, Void> {

        private boolean canStart = true;
        private boolean disconnected = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Connection connection = Connection.getInstance(addr);
            connection.openConnection();

            String signalLevel = connection.getSignalLevel();
            String totalData = connection.getSessionData();
            String users = connection.getCurrentUsers();

            try {
                curUserCount = Integer.parseInt(users);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            String time = connection.getConnectTime();

            if (signalLevel == null || signalLevel == "") {

                builder.setContentText(getResources().getText(R.string.No_signal));
                startForeground(NOTIFICATION_ID, builder.build());
            } else if (signalLevel.equals(Connection.NOT_SUPPORTED) || signalLevel.equals(Connection.NOT_CONNECTED)) {

                canStart = false;
                disconnected = false;
                stopSelf();
            } else if (signalLevel.equals(Connection.DISCONNECTED)) {
                canStart = false;
                disconnected = true;
                stopSelf();

            } else {
                builder.setContentText(getResources().getText(R.string.Signal_level) + ": " + signalLevel + " " + getResources().getText(R.string.Total_data) + ": " + totalData)
                        .setSubText(getResources().getText(R.string.Users) + ": " + users + " " + getResources().getText(R.string.Time) + " " + time);
                startForeground(NOTIFICATION_ID, builder.build());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (curUserCount > prevUserCount) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(SignalNotifyService.this, NOTIFICATION_CHANNEL_NEW_USER);

                Notification notification = builder.setTicker(getResources().getText(R.string.new_device_connected))
                        .setSmallIcon(R.drawable.ic_echonest)
                        .setContentTitle(getResources().getText(R.string.new_device_connected))
                        .setContentText(getResources().getText(R.string.new_device_connected_notification_content))
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND).build();

                NotificationManagerCompat.from(SignalNotifyService.this).notify(NOTIFICATION_ID_NEW_USER, notification);
                prevUserCount = curUserCount;
            } else if (curUserCount < prevUserCount) {
                prevUserCount = curUserCount;
            }

            if (canStart) {

                if (!stopTask)
                    runTask();
            } else if (!disconnected) {
                Toast.makeText(getApplicationContext(), R.string.router_is_not_connected_or_not_supported, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


