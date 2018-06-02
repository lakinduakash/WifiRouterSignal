package app.ultimatex.wifiroutersignal;


import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class SignalNotifyService extends Service {

    public static final int NOTIFICATION_ID = 5;
    private static int count = 0;

    private NotificationCompat.Builder builder;
    private NotificationUpdater u;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        builder = new NotificationCompat.Builder(this, "MY_CHANNEL")
                .setContentText("Started")
                .setSmallIcon(R.drawable.ic_network_check_white_24dp)
                .setContentTitle("Connected")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        runTask();

        return super.onStartCommand(intent, flags, startId);

    }


    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }


    @Override
    public boolean stopService(Intent name) {
        if (u != null)
            u.cancel(true);


        return super.stopService(name);
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Connection connection = Connection.getInstance();

            String signalLevel = connection.getSignalLevel();
            String totalData = connection.getSessionData();

            if (signalLevel == null || signalLevel == "") {

                builder.setContentText("No Signal");
                startForeground(NOTIFICATION_ID, builder.build());
            } else if (signalLevel.equals(Connection.NOT_SUPPORTED)) {

                canStart = false;
                stopSelf();
            } else {
                builder.setContentText("Signal level:" + " " + signalLevel + " Total Data: " + totalData);
                startForeground(NOTIFICATION_ID, builder.build());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (canStart) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isMyServiceRunning(SignalNotifyService.class))
                    runTask();
            } else {
                Toast.makeText(getApplicationContext(), "Your router is not supported or you are not connected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


