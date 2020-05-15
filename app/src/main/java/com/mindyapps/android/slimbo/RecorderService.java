package com.mindyapps.android.slimbo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mindyapps.android.slimbo.ui.sleeping.SleepingActivity;

public class RecorderService extends Service {

    public static final String CHANNEL_ID = "ForegroundRecorderServiceChannel";
    public static final String START_ACTION = "StopRecording";
    public static final String STOP_ACTION = "StartRecording";
    private Boolean isActive;

    public RecorderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(START_ACTION)) {
            isActive = true;
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Foreground Service")
                    .setSmallIcon(R.drawable.ic_sleep)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);

            new Thread() {
                public void run() {
                    arm();
                }
            }.start();

        } else if (intent.getAction().equals(STOP_ACTION)){
            isActive = false;
            Intent sleepingIntent = new Intent(SleepingActivity.RECEIVER_INTENT);
            sleepingIntent.putExtra(SleepingActivity.RECEIVER_MESSAGE, "stop");
            LocalBroadcastManager.getInstance(this).sendBroadcast(sleepingIntent);
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    public void arm() {
        while (isActive) {
            Log.d("qwwe", "active");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Recording Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
