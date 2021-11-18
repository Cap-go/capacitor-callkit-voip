package com.bfine.capactior.callkitvoip.androidcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.bfine.capactior.callkitvoip.CallKitVoipPlugin;


public class VoipForegroundServiceActionReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null ) {
            String action = intent.getAction();
            String token = intent.getStringExtra("token");
            String roomName = intent.getStringExtra("roomName");
            String username = intent.getStringExtra("username");


            if (action != null) {
                performClickAction(context, action,token,roomName,username);
            }

            // Close the notification after the click action is performed.


        }
    }
    private void performClickAction(Context context, String action,String token,String roomName,String username) {
        Log.d("performClickAction","action "+action + "   "+username);

        if (action.equals("RECEIVE_CALL")) {

            Intent dialogIntent = new Intent(context, CallActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.putExtra("auto_answer",true);
            dialogIntent.putExtra("token",token);
            dialogIntent.putExtra("roomName",roomName);
            dialogIntent.putExtra("username",username);

            context.startActivity(dialogIntent);
        }
        else if (action.equals("FULLSCREEN_CALL")) {


            Intent dialogIntent = new Intent(context, CallActivity.class);

            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.putExtra("auto_answer",false);
            dialogIntent.putExtra("token",token);
            dialogIntent.putExtra("roomName",roomName);
            dialogIntent.putExtra("username",username);

            context.startActivity(dialogIntent);
        }
        else if (action.equals("CANCEL_CALL")) {
            context.stopService(new Intent(context, VoipForegroundService.class));
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
            end_call(username,roomName);

        }
    }

    public void end_call( String username,String connectionId)
    {
        CallKitVoipPlugin instance = CallKitVoipPlugin.getInstance();
        instance.notifyEvent("RejectCall",username,connectionId);


    }

}