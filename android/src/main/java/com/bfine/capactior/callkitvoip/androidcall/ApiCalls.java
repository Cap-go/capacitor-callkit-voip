package com.bfine.capactior.callkitvoip.androidcall;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;


public class ApiCalls {

    public void gettwiliotoken(final String connectionId,final RetreivedTokenCallback callback)
    {
        Log.d("gettwiliotoken","called " + connectionId);

            AndroidNetworking.get("http://us-central1-bettercall-app.cloudfunctions.net/getTokenTwilioVideo?name=operator&token="+connectionId)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener()
                    {
                        @Override
                        public void onResponse(JSONObject response)
                        {
                            try {
                                Log.d("gettwiliotoken","called " + response);

                                String token = response.getString("token");
                                Log.d("token",token);
                                callback.onTokenRetreived(token);
                            } catch (JSONException e) {
                                Log.d("gettwiliotoken",e.toString());

                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError error)
                        {
                            Log.d("gettwiliotoken",error.toString());

                        }
                    });

    }
}