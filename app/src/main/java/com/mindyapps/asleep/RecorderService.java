package com.mindyapps.asleep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.mindyapps.asleep.data.model.Factor;
import com.mindyapps.asleep.data.model.Music;
import com.mindyapps.asleep.preferences.SleepingStore;
import com.mindyapps.asleep.ui.sleeping.SleepingActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.mindyapps.asleep.ui.sleeping.SleepingActivity.AUDIO_RECORDS;
import static com.mindyapps.asleep.ui.sleeping.SleepingActivity.END_TIME;
import static com.mindyapps.asleep.ui.sleeping.SleepingActivity.FROM_ALARM;
import static com.mindyapps.asleep.ui.sleeping.SleepingActivity.SELECTED_FACTORS;
import static com.mindyapps.asleep.ui.sleeping.SleepingActivity.START_TIME;

public class RecorderService extends Service {

    public static final String CHANNEL_ID = "ForegroundRecorderServiceChannel";
    public static final String START_ACTION = "StopRecording";
    public static final String STOP_ACTION = "StartRecording";
    private int RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int RECORDER_SAMPLERATE = 23100;
    private int FIVE_MIN_BYTES = 26460000;
    private int THREE_MIN_BYTES = 8700000;
    private int ONE_MIN_BYTES = 5292000;
    private int THIRTY_SEC_BYTES = 2646000;
    private byte RECORDER_BPP = (byte) 16;
    private int minVolumeLevel = 8;
    private int resID;

    private boolean isActive;
    private boolean signalCompleted = true;
    private boolean forceSave = false;
    private boolean isSaving;
    private boolean stop;
    private Vibrator v;
    private Handler timeHandler, signalHandler;
    private MediaPlayer player;
    private SleepingStore sleepingStore;
    private Music selectedSignal;
    private ArrayList<Factor> factors;
    private ArrayList<com.mindyapps.asleep.data.model.AudioRecord> audioRecords = new ArrayList<>();
    private long startSleepingTime;

    private PowerManager.WakeLock wakeLock;

    public RecorderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sleepingStore = new SleepingStore(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()));
        if (!sleepingStore.getUseVibration()) {
            selectedSignal = new Gson().fromJson(sleepingStore.getAntiSnoreSound(), Music.class);
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getCanonicalName());
        }
        wakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction().equals(START_ACTION)) {
            isActive = true;
            factors = intent.getParcelableArrayListExtra(SELECTED_FACTORS);
            startSleepingTime = System.currentTimeMillis();

            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.monitoring))
                    .setSmallIcon(R.drawable.ic_sleep)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);

            new Thread() {
                public void run() {
                    arm();
                }
            }.start();
            timeHandler = new Handler();
            signalHandler = new Handler();
            timeHandler.postDelayed(minTimeTask, 5000);

        } else if (intent.getAction().equals(STOP_ACTION)) {
            isActive = false;
            Intent sleepingIntent = new Intent(SleepingActivity.RECEIVER_INTENT);
            sleepingIntent.putExtra(SleepingActivity.RECEIVER_MESSAGE, "stop");
            sleepingIntent.putExtra(SELECTED_FACTORS, factors);
            sleepingIntent.putExtra(AUDIO_RECORDS, audioRecords);
            sleepingIntent.putExtra(START_TIME, startSleepingTime);
            sleepingIntent.putExtra(FROM_ALARM, intent.getBooleanExtra(FROM_ALARM, false));
            sleepingIntent.putExtra(END_TIME, System.currentTimeMillis());
            LocalBroadcastManager.getInstance(this).sendBroadcast(sleepingIntent);
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    Runnable minTimeTask = new Runnable() {
        public void run() {
            if (!stop) {
                sleepingStore.setMinimalTimeReached(true);
            }
        }
    };

    Runnable stopPlayerTask = new Runnable() {
        public void run() {
            player.stop();
            signalCompleted = true;
        }
    };

    Runnable stopVibrationTask = new Runnable() {
        public void run() {
            v.cancel();
            signalCompleted = true;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (timeHandler != null) {
            stop = true;
            timeHandler.removeCallbacks(minTimeTask);
            signalHandler.removeCallbacks(stopPlayerTask);
            if (player != null) {
                player.stop();
            }
        }
        if (!sleepingStore.getMinimalTimeReached()) {
            for (int i = 0; i < audioRecords.size(); i++) {
                String filePath = audioRecords.get(i).getFile_name();
                File fdelete = new File(filePath);
                fdelete.delete();
            }
        }
        sleepingStore.setMinimalTimeReached(false);
    }

    public void arm() {
        // Get the minimum buffer size required for the successful creation of an AudioRecord object.
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        // Get the minimum buffer size required for the successful creation of an AudioRecord object.
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);

        // Initialize Audio Recorder.
        AudioRecord audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSizeInBytes);

        // Start Recording.
        audioRecorder.startRecording();

        long silentSeconds = 0;

        int numberOfReadBytes = 0;
        byte audioBuffer[] = new byte[bufferSizeInBytes];
        boolean recording = false;
        float tempFloatBuffer[] = new float[3];
        int tempIndex = 0;
        int totalReadBytes = 0;
        byte totalByteBuffer[] = new byte[60 * 6 * 44100 * 2];

        // While data come from microphone.
        while (isActive) {
            synchronized (this) {
                float totalAbsValue = 0.0f;
                short sample = 0;

                numberOfReadBytes = audioRecorder.read(audioBuffer, 0, bufferSizeInBytes);

                // Analyze Sound.
                for (int i = 0; i < bufferSizeInBytes; i += 2) {
                    sample = (short) ((audioBuffer[i]) | audioBuffer[i + 1] << 8);
                    totalAbsValue += Math.abs(sample) / (numberOfReadBytes / 2);
                }


                // Analyze temp buffer.
                tempFloatBuffer[tempIndex % 3] = totalAbsValue;
                float temp = 0.0f;
                for (int i = 0; i < 3; ++i)
                    temp += tempFloatBuffer[i];

                if ((temp >= 0 && temp <= minVolumeLevel) && !recording && !isSaving) {
                    tempIndex++;
                    continue;
                }

                if (temp > minVolumeLevel && !recording && !isSaving) {
                    Log.d("qwwe", "got sound");
                    if (sleepingStore.getUseAntiSnore() && sleepingStore.getMinimalTimeReached() && signalCompleted) {
                        if (!sleepingStore.getUseVibration()) {
                            resID = getApplicationContext().getResources()
                                    .getIdentifier(selectedSignal.getFileName(), "raw",
                                            getApplicationContext().getPackageName());
                            player = MediaPlayer.create(getApplicationContext(), resID);
                            player.setLooping(true);
                            player.start();
                            Log.d("qwwe", "setting handler");
                            signalHandler.postDelayed(stopPlayerTask, sleepingStore.getAntiSnoreDuration() * 1000);
                            signalCompleted = false;
                        } else {
                            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            signalHandler.postDelayed(stopVibrationTask, sleepingStore.getAntiSnoreDuration() * 1000);
                            signalCompleted = false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(sleepingStore.getAntiSnoreDuration() * 1000, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                v.vibrate(sleepingStore.getAntiSnoreDuration() * 1000);
                            }
                        }
                    } else {
                        recording = true;
                    }
                }

                if (totalReadBytes >= (THREE_MIN_BYTES)) {
                    Log.d("qwwe", "totalReadBytes " + totalReadBytes);
                    forceSave = true;
                }

                if (temp > minVolumeLevel && recording) {
                    silentSeconds = 0;
                }

                if (((temp >= 0 && temp <= minVolumeLevel) && recording && !isSaving) || forceSave) {
                    silentSeconds++;
                    if (silentSeconds >= 160 || forceSave) {
                        isSaving = true;
                        Log.d("qwwe", "Save audio to file.");
                        // Save audio to file.
                        String filepath = getApplicationContext().getExternalCacheDir().getPath();
                        File file = new File(filepath, "AudioRecorder");
                        if (!file.exists())
                            file.mkdirs();

                        String fn = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav";

                        long totalAudioLen = 0;
                        long totalDataLen = totalAudioLen + 36;
                        long longSampleRate = RECORDER_SAMPLERATE;
                        int channels = 1;
                        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;
                        totalAudioLen = totalReadBytes;
                        totalDataLen = totalAudioLen + 36;
                        byte finalBuffer[] = new byte[totalReadBytes + 44];

                        totalDataLen -= 420000;
                        totalAudioLen -= 420000;
                        totalReadBytes -= 420000;

                        finalBuffer[0] = 'R'; // RIFF/WAVE header
                        finalBuffer[1] = 'I';
                        finalBuffer[2] = 'F';
                        finalBuffer[3] = 'F';
                        finalBuffer[4] = (byte) (totalDataLen & 0xff);
                        finalBuffer[5] = (byte) ((totalDataLen >> 8) & 0xff);
                        finalBuffer[6] = (byte) ((totalDataLen >> 16) & 0xff);
                        finalBuffer[7] = (byte) ((totalDataLen >> 24) & 0xff);
                        finalBuffer[8] = 'W';
                        finalBuffer[9] = 'A';
                        finalBuffer[10] = 'V';
                        finalBuffer[11] = 'E';
                        finalBuffer[12] = 'f'; // 'fmt ' chunk
                        finalBuffer[13] = 'm';
                        finalBuffer[14] = 't';
                        finalBuffer[15] = ' ';
                        finalBuffer[16] = 16; // 4 bytes: size of 'fmt ' chunk
                        finalBuffer[17] = 0;
                        finalBuffer[18] = 0;
                        finalBuffer[19] = 0;
                        finalBuffer[20] = 1; // format = 1
                        finalBuffer[21] = 0;
                        finalBuffer[22] = (byte) channels;
                        finalBuffer[23] = 0;
                        finalBuffer[24] = (byte) (longSampleRate & 0xff);
                        finalBuffer[25] = (byte) ((longSampleRate >> 8) & 0xff);
                        finalBuffer[26] = (byte) ((longSampleRate >> 16) & 0xff);
                        finalBuffer[27] = (byte) ((longSampleRate >> 24) & 0xff);
                        finalBuffer[28] = (byte) (byteRate & 0xff);
                        finalBuffer[29] = (byte) ((byteRate >> 8) & 0xff);
                        finalBuffer[30] = (byte) ((byteRate >> 16) & 0xff);
                        finalBuffer[31] = (byte) ((byteRate >> 24) & 0xff);
                        finalBuffer[32] = (byte) (2 * 16 / 8); // block align
                        finalBuffer[33] = 0;
                        finalBuffer[34] = RECORDER_BPP; // bits per sample
                        finalBuffer[35] = 0;
                        finalBuffer[36] = 'd';
                        finalBuffer[37] = 'a';
                        finalBuffer[38] = 't';
                        finalBuffer[39] = 'a';
                        finalBuffer[40] = (byte) (totalAudioLen & 0xff);
                        finalBuffer[41] = (byte) ((totalAudioLen >> 8) & 0xff);
                        finalBuffer[42] = (byte) ((totalAudioLen >> 16) & 0xff);
                        finalBuffer[43] = (byte) ((totalAudioLen >> 24) & 0xff);

                        for (int i = 0; i < totalReadBytes; ++i)
                            finalBuffer[44 + i] = totalByteBuffer[i];

                        FileOutputStream out;
                        try {
                            out = new FileOutputStream(fn);
                            try {
                                Log.d("qwwe", "totalReadBytes: " + totalReadBytes);
                                out.write(finalBuffer);
                                out.close();
                                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                mediaMetadataRetriever.setDataSource(fn);
                                String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                if (duration == null || Long.parseLong(duration) < 5000) {
                                    File fdelete = new File(fn);
                                    fdelete.delete();
                                } else {
                                    com.mindyapps.asleep.data.model.AudioRecord audioRecord =
                                            new com.mindyapps.asleep.data.model.AudioRecord(null,
                                                    fn, Long.parseLong(duration), System.currentTimeMillis());
                                    audioRecords.add(audioRecord);
                                }
                                forceSave = false;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }

                        // */
                        tempIndex++;
                        isSaving = false;
                        recording = false;
                        silentSeconds = 0;
                        numberOfReadBytes = 0;
                        tempIndex = 0;
                        totalReadBytes = 0;
                    }
                }


                /*
                 // -> Recording sound here

                //
                */

                // -> Recording sound here
                if (!sleepingStore.getUseAntiSnore() && totalReadBytes <= (THREE_MIN_BYTES)) {
                    Log.d("qwwe", "Recording Sound.");
                    if (numberOfReadBytes >= 0)
                        System.arraycopy(audioBuffer, 0, totalByteBuffer, totalReadBytes, numberOfReadBytes);
                    totalReadBytes += numberOfReadBytes;
                }
                // */


                tempIndex++;
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
