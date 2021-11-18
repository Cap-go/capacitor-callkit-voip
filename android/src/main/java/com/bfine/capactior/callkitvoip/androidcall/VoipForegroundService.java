package com.bfine.capactior.callkitvoip.androidcall;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bfine.capactior.callkitvoip.R;

import java.util.Objects;

public class VoipForegroundService extends Service {
    private String INCOMING_CHANNEL_ID = "IncomingCallChannel";
    private String INCOMING_CHANNEL_NAME = "Incoming Call Channel";
    private String ONGOING_CHANNEL_ID = "OngoingCallChannel";
    private String ONGOING_CHANNEL_NAME = "Ongoing Call Channel";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder notificationBuilder;
    public static MediaPlayer ringtone;
    public static Vibrator vibrator;
    String username="",token="",roomName="";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop_ringtone();
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d("VoipForegroundService","onStartCommand "+action);
        switch (action)
        {
            case "incoming":
                build_incoming_call_notification(intent);
                break;
            case "answered":
                build_answered_call_notification();
                break;

        }
        return START_NOT_STICKY;
    }



    public void build_incoming_call_notification(Intent intent)
    {

        ringtone = new MediaPlayer();
        username = intent.getStringExtra("username");
        token = intent.getStringExtra("token");
        roomName = intent.getStringExtra("roomName");
        Log.d("VoipForegroundService","build_incoming_call_notification for "+username);

        try {
            Intent receiveCallAction = new Intent(getApplicationContext(), VoipForegroundServiceActionReceiver.class);
            receiveCallAction.putExtra("token",token);
            receiveCallAction.putExtra("roomName",roomName);
            receiveCallAction.putExtra("username",username);

            receiveCallAction.setAction("RECEIVE_CALL");

            Intent cancelCallAction = new Intent(getApplicationContext(), VoipForegroundServiceActionReceiver.class);
            cancelCallAction.putExtra("token",token);
            cancelCallAction.putExtra("roomName",roomName);
            cancelCallAction.putExtra("username",username);

            cancelCallAction.setAction("CANCEL_CALL");

            Intent fullscreenCallAction = new Intent(getApplicationContext(), VoipForegroundServiceActionReceiver.class);
            fullscreenCallAction.putExtra("token",token);
            fullscreenCallAction.putExtra("roomName",roomName);
            fullscreenCallAction.putExtra("username",username);

            fullscreenCallAction.setAction("FULLSCREEN_CALL");

            PendingIntent receiveCallPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cancelCallPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent fullscreenCallPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1202, fullscreenCallAction, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder = null;
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder = new NotificationCompat.Builder(this, INCOMING_CHANNEL_ID)
                    .setContentTitle(username)
                    .setContentText(getString(R.string.incoming_call))
                    .setSmallIcon(R.drawable.ic_call_black_24dp)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setSound(alarmSound)

                    .addAction(new NotificationCompat.Action.Builder(
                            0,
                            getString(R.string.reject),
                            cancelCallPendingIntent).build())
                    .addAction(new NotificationCompat.Action.Builder(
                            0,
                            getString(R.string.answer),
                            receiveCallPendingIntent).build())
                    .setAutoCancel(false)

                    .setFullScreenIntent(fullscreenCallPendingIntent, true);
            long[] pattern = {0, 100, 1000, 300};
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            AudioManager am = (AudioManager) VoipForegroundService.this.getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
            {
                vibrator.vibrate(pattern, 0);
            }
            Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.call);
            try
            {
                ringtone.setDataSource(getApplicationContext(), sound);
                ringtone.setAudioStreamType(AudioManager.STREAM_RING);
                ringtone.prepare();
                ringtone.setLooping(true);
                ringtone.start();

            }
            catch (Exception e)
            {
                Log.d("VoipForegroundService","1 "+e.toString());

            }

            createIncomingChannel();
            startForeground(120, notificationBuilder.build());



        } catch (Exception e) {
            e.printStackTrace();
            Log.d("VoipForegroundService","2 "+e.toString());

        }

    }
    public void build_answered_call_notification()
    {
        stop_ringtone();
        stopSelf();
    }


    public void stop_ringtone()
    {
        try {
            ringtone.stop();
            ringtone.release();
            ringtone = null;
            vibrator.cancel();

        }
        catch (Exception e)
        {

        }
    }
    public void createOngoingChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(ONGOING_CHANNEL_ID, ONGOING_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(ONGOING_CHANNEL_NAME);

            channel.setSound(null,null);
            Objects.requireNonNull(getApplicationContext().getSystemService(NotificationManager.class)).createNotificationChannel(channel);

        }
    }
    public void createIncomingChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(INCOMING_CHANNEL_ID, INCOMING_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(INCOMING_CHANNEL_NAME);

            channel.setSound(null,null);


            Objects.requireNonNull(getApplicationContext().getSystemService(NotificationManager.class)).createNotificationChannel(channel);

        }
    }

}