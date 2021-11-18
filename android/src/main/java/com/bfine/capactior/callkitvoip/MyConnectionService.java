package com.bfine.capactior.callkitvoip;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import android.telecom.TelecomManager;
import android.os.Handler;
import android.net.Uri;
import java.util.ArrayList;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.getcapacitor.Logger;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MyConnectionService extends ConnectionService {

    private static String TAG = "MyConnectionService";
    private static Connection conn;

    public static Connection getConnection() {
        return conn;
    }

    public static void deinitConnection() {
        conn = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Connection onCreateIncomingConnection(final PhoneAccountHandle connectionManagerPhoneAccount, final ConnectionRequest request) {
        final Connection connection = new Connection() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onAnswer() {
                this.setActive();
                CallKitVoipPlugin plugin = CallKitVoipPlugin.getInstance();



                if(plugin != null)
                    plugin.notifyEvent("callAnswered",
                            request.getExtras().getString("username"),
                            request.getExtras().getString("connectionId")
                    );


                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("app.bettercall");
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }

                this.setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));

            }

            @Override
            public void onReject() {
                DisconnectCause cause = new DisconnectCause(DisconnectCause.REJECTED);
                this.setDisconnected(cause);
                this.destroy();
                conn = null;
//                ArrayList<CallbackContext> callbackContexts = AndroidCall.getCallbackContexts().get("reject");
//                for (final CallbackContext callbackContext : callbackContexts) {
//                    AndroidCall.getCordova().getThreadPool().execute(new Runnable() {
//                        public void run() {
//                            PluginResult result = new PluginResult(PluginResult.Status.OK, "reject event called successfully");
//                            result.setKeepCallback(true);
//                            callbackContext.sendPluginResult(result);
//                        }
//                    });
//                }
            }

            @Override
            public void onAbort() {
                super.onAbort();
            }

            @Override
            public void onDisconnect() {
                DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
                this.setDisconnected(cause);
                this.destroy();
                conn = null;
//                ArrayList<CallbackContext> callbackContexts = AndroidCall.getCallbackContexts().get("hangup");
//                for (final CallbackContext callbackContext : callbackContexts) {
//                    AndroidCall.getCordova().getThreadPool().execute(new Runnable() {
//                        public void run() {
//                            PluginResult result = new PluginResult(PluginResult.Status.OK, "hangup event called successfully");
//                            result.setKeepCallback(true);
//                            callbackContext.sendPluginResult(result);
//                        }
//                    });
//                }
            }
        };
        connection.setAddress(Uri.parse(request.getExtras().getString("from")), TelecomManager.PRESENTATION_ALLOWED);
//        Icon icon = AndroidCall.getIcon();
//        if(icon != null) {
//            StatusHints statusHints = new StatusHints((CharSequence)"", icon, new Bundle());
//            connection.setStatusHints(statusHints);
//        }
        conn = connection;
//        ArrayList<CallbackContext> callbackContexts = AndroidCall.getCallbackContexts().get("receiveCall");
//        for (final CallbackContext callbackContext : callbackContexts) {
//            AndroidCall.getCordova().getThreadPool().execute(new Runnable() {
//                public void run() {
//                    PluginResult result = new PluginResult(PluginResult.Status.OK, "receiveCall event called successfully");
//                    result.setKeepCallback(true);
//                    callbackContext.sendPluginResult(result);
//                }
//            });
//        }
        return connection;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        final Connection connection = new Connection() {
            @Override
            public void onAnswer() {
                super.onAnswer();
            }

            @Override
            public void onReject() {
                super.onReject();
            }

            @Override
            public void onAbort() {
                super.onAbort();
            }

            @Override
            public void onDisconnect() {
                DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
                this.setDisconnected(cause);
                this.destroy();
                conn = null;
//                ArrayList<CallbackContext> callbackContexts = AndroidCall.getCallbackContexts().get("hangup");
//                for (final CallbackContext callbackContext : callbackContexts) {
//                    AndroidCall.getCordova().getThreadPool().execute(new Runnable() {
//                        public void run() {
//                            PluginResult result = new PluginResult(PluginResult.Status.OK, "hangup event called successfully");
//                            result.setKeepCallback(true);
//                            callbackContext.sendPluginResult(result);
//                        }
//                    });
//                }
            }

            @Override
            public void onStateChanged(int state) {
                if(state == Connection.STATE_DIALING) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            Intent intent = new Intent(AndroidCall.getCapacitor().getActivity().getApplicationContext(), AndroidCall.getCordova().getActivity().getClass());
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                            AndroidCall.getCapacitor().getActivity().getApplicationContext().startActivity(intent);
                        }
                    }, 500);
                }
            }
        };
        connection.setAddress(Uri.parse(request.getExtras().getString("to")), TelecomManager.PRESENTATION_ALLOWED);
//        Icon icon = AndroidCall.getIcon();
//        if(icon != null) {
//            StatusHints statusHints = new StatusHints((CharSequence)"", icon, new Bundle());
//            connection.setStatusHints(statusHints);
//        }
        connection.setDialing();
        conn = connection;
//        ArrayList<CallbackContext> callbackContexts = AndroidCall.getCallbackContexts().get("sendCall");
//        if(callbackContexts != null) {
//            for (final CallbackContext callbackContext : callbackContexts) {
//                AndroidCall.getCordova().getThreadPool().execute(new Runnable() {
//                    public void run() {
//                        PluginResult result = new PluginResult(PluginResult.Status.OK, "sendCall event called successfully");
//                        result.setKeepCallback(true);
//                        callbackContext.sendPluginResult(result);
//                    }
//                });
//            }
//        }
        return connection;
    }
}