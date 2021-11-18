package com.bfine.capactior.callkitvoip.androidcall;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


public class VoipBackgroundService extends Service
{
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("MySipService", "onStartCommand");
        if(intent.hasExtra("connectionId") && intent.hasExtra("username"))
        {
            String connectionId = intent.getStringExtra("connectionId");
            String username = intent.getStringExtra("username");
            ApiCalls apiCalls =  new ApiCalls();
            apiCalls.gettwiliotoken(connectionId, new RetreivedTokenCallback() {
                @Override
                public void onTokenRetreived(String token) {
                    Log.d("onTokenRetreived",token);
                    if(!isServiceRunningInForeground(VoipBackgroundService.this,VoipForegroundService.class)) {
                        show_call_notification("incoming",token,username,connectionId);

                        KeyguardManager km = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
                        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("name");
                        kl.disableKeyguard();
                    }

                }
            });
        }
        return START_NOT_STICKY;
    }



    public void show_call_notification(String action, String token,String username,String roomName)
    {
        Log.d("show_call_notification",action);
        Intent serviceIntent = new Intent(this, VoipForegroundService.class);
        serviceIntent.setAction(action);
        serviceIntent.putExtra("token",token);
        serviceIntent.putExtra("username",username);
        serviceIntent.putExtra("roomName",roomName);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            VoipBackgroundService.this.startForegroundService(serviceIntent);

        } else {
            VoipBackgroundService.this.startService(serviceIntent);
        }
    }



    @Override
    public void onCreate()
    {
        super.onCreate();




    }

}