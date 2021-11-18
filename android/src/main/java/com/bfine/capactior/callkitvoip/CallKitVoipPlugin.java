package com.bfine.capactior.callkitvoip;

import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginHandle;
import com.getcapacitor.PluginMethod;
import com.google.firebase.messaging.FirebaseMessaging;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class CallKitVoipPlugin extends Plugin {
    public  static  Bridge                      staticBridge = null;
    public          MyFirebaseMessagingService  messagingService;


    @Override
    public void load(){
        staticBridge   = this.bridge;


        this.getActivity().getApplicationContext();

    }

    @PluginMethod
    public void register(PluginCall call) {
        final String topicName = call.getString("userToken");
        Log.d("CallKitVoip","register");

        if(topicName == null){
            call.reject("Topic name hasn't been specified correctly");
            return;
        }
        FirebaseMessaging
                .getInstance()
                .subscribeToTopic(topicName)
                .addOnSuccessListener(unused -> {
                    JSObject ret = new JSObject();
                    Logger.debug("CallKit: Subscribed");
                    ret.put("message", "Subscribed to topic " + topicName);
                    call.resolve(ret);

                })
                .addOnFailureListener(e -> {
                    Logger.debug("CallKit: Cannot subscribe");
                    call.reject("Cant subscribe to topic" + topicName);
                });
        call.resolve();
    }
    public void notifyEvent(String eventName, String username, String connectionId){
        Log.d("notifyEvent",eventName + "  " + username + "   " + connectionId);

//        JSObject data = new JSObject();
//        data.put("username",        username);
//        data.put("connectionId",    connectionId);
//        notifyListeners("callAnswered", data);
    }



    public static CallKitVoipPlugin getInstance() {
        if (staticBridge == null || staticBridge.getWebView() == null)
            return  null;

        PluginHandle handler = staticBridge.getPlugin("CallKitVoip");

        return handler == null
                ? null
                : (CallKitVoipPlugin) handler.getInstance();
    }

}
