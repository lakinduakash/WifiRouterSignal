package app.ultimatex.wifiroutersignal;


import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class SignalNotifyService extends Service {

    public static final int NOTIFICATION_ID=5;
    private static int count=0;

    NotificationCompat.Builder builder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        new NotificationUpdater().execute();
        return super.onStartCommand(intent, flags, startId);

    }


    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }



    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    class NotificationUpdater extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {


        }
    }
}


