package com.bfine.capactior.callkitvoip.androidcall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bfine.capactior.callkitvoip.CallKitVoipPlugin;
import com.bfine.capactior.callkitvoip.R;
import com.bfine.capactior.callkitvoip.androidcall.util.CameraCapturerCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.twilio.audioswitch.AudioDevice;
import com.twilio.audioswitch.AudioDevice.Earpiece;
import com.twilio.audioswitch.AudioDevice.Speakerphone;
import com.twilio.audioswitch.AudioDevice.WiredHeadset;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.video.AudioCodec;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.G722Codec;
import com.twilio.video.H264Codec;
import com.twilio.video.IsacCodec;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.OpusCodec;
import com.twilio.video.PcmaCodec;
import com.twilio.video.PcmuCodec;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;
import com.twilio.video.Vp8Codec;
import com.twilio.video.Vp9Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Unit;
import tvi.webrtc.VideoSink;


public class CallActivity extends AppCompatActivity {
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_INDEX = 0;
    private static final int MIC_PERMISSION_INDEX = 1;
    private static final String TAG = "VideoActivity";
    boolean thumb_for_local = true;

    /*
     * Audio and video tracks can be created with names. This feature is useful for categorizing
     * tracks of participants. For example, if one participant publishes a video track with
     * ScreenCapturer and CameraCapturer with the names "screen" and "camera" respectively then
     * other participants can use RemoteVideoTrack#getName to determine which video track is
     * produced from the other participant's screen or camera.
     */
    private static final String LOCAL_AUDIO_TRACK_NAME = "mic";
    private static final String LOCAL_VIDEO_TRACK_NAME = "camera";


    /*
     * Access token used to connect. This field will be set either from the console generated token
     * or the request to the token server.
     */
    private String accessToken;
    private String roomName;
    private String username;

    private Boolean auto_answer;


    /*
     * A Room represents communication between a local participant and one or more participants.
     */
    private Room room;
    private LocalParticipant localParticipant;

    /*
     * AudioCodec and VideoCodec represent the preferred codec for encoding and decoding audio and
     * video.
     */
    private AudioCodec audioCodec;
    private VideoCodec videoCodec;

    /*
     * Encoding parameters represent the sender side bandwidth constraints.
     */
    private EncodingParameters encodingParameters;

    /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
    private VideoView primaryVideoView;
    private VideoView thumbnailVideoView;

    /*
     * Android shared preferences used for settings
     */
    private SharedPreferences preferences;

    /*
     * Android application UI elements
     */
    private CameraCapturerCompat cameraCapturerCompat;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private FloatingActionButton connectActionFab;
    private FloatingActionButton switchCameraActionFab;
    private FloatingActionButton localVideoActionFab;
    private FloatingActionButton muteActionFab;
    private FloatingActionButton answer;
    private FloatingActionButton reject;

    private ProgressBar reconnectingProgressBar;
    private TextView callerUser;
    private ImageView bcg;
    private LinearLayout answered_actions,unanswered_actions;
    private String remoteParticipantIdentity;

    /*
     * Audio management
     */
    private AudioSwitch audioSwitch;
    private int savedVolumeControlStream;

    private VideoSink localVideoView;
    private boolean disconnectedFromOnDestroy;
    private boolean enableAutomaticSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        primaryVideoView = findViewById(R.id.primary_video_view);
        thumbnailVideoView = findViewById(R.id.thumbnail_video_view);
        reconnectingProgressBar = findViewById(R.id.reconnecting_progress_bar);
        callerUser = findViewById(R.id.callerUser);
        bcg = findViewById(R.id.bcg);
        thumbnailVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch_views();
            }
        });
        primaryVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch_views();
            }
        });
        connectActionFab = findViewById(R.id.connect_action_fab);
        switchCameraActionFab = findViewById(R.id.switch_camera_action_fab);
        localVideoActionFab = findViewById(R.id.local_video_action_fab);
        muteActionFab = findViewById(R.id.mute_action_fab);
        answer = findViewById(R.id.answer);
        reject = findViewById(R.id.reject);

        accessToken = getIntent().getStringExtra("token");
        roomName = getIntent().getStringExtra("roomName");
        answered_actions = findViewById(R.id.answered_actions);
        unanswered_actions = findViewById(R.id.unanswered_actions);
        if(getIntent().hasExtra("auto_answer") &&  getIntent().hasExtra("username")) {
            auto_answer = getIntent().getBooleanExtra("auto_answer", false);
            username = getIntent().getStringExtra("username");
        }
        /*
         * Get shared preferences to read settings
         */
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        /*
         * Setup audio management and set the volume control stream
         */
        audioSwitch = new AudioSwitch(getApplicationContext());
        savedVolumeControlStream = getVolumeControlStream();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        /*
         * Check camera and microphone permissions. Needed in Android M. Also, request for bluetooth
         * permissions for enablement of bluetooth audio routing.
         */
        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraMicrophoneAndBluetooth();
        } else {
            audioSwitch.start(
                    (audioDevices, audioDevice) -> Unit.INSTANCE);
            createAudioAndVideoTracks();
        }

        /*
         * Set the initial state of the UI
         */

        intializeUI();
        audioCodec =
                getAudioCodecPreference(
                        SettingsActivity.PREF_AUDIO_CODEC,
                        SettingsActivity.PREF_AUDIO_CODEC_DEFAULT);
        videoCodec =
                getVideoCodecPreference(
                        SettingsActivity.PREF_VIDEO_CODEC,
                        SettingsActivity.PREF_VIDEO_CODEC_DEFAULT);
        enableAutomaticSubscription =
                getAutomaticSubscriptionPreference(
                        SettingsActivity.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION,
                        SettingsActivity.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION_DEFAULT);
        /*
         * Get latest encoding parameters
         */
        final EncodingParameters newEncodingParameters = getEncodingParameters();

        /*
         * If the local video track was released when the app was put in the background, recreate.
         */
        if (localVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
            localVideoTrack =
                    LocalVideoTrack.create(
                            this, true, cameraCapturerCompat, LOCAL_VIDEO_TRACK_NAME);
            localVideoTrack.addSink(localVideoView);

            /*
             * If connected to a Room then share the local video track.
             */
            if (localParticipant != null) {
                localParticipant.publishTrack(localVideoTrack);

                /*
                 * Update encoding parameters if they have changed.
                 */
                if (!newEncodingParameters.equals(encodingParameters)) {
                    localParticipant.setEncodingParameters(newEncodingParameters);
                }
            }
        }

        /*
         * Update encoding parameters
         */
        encodingParameters = newEncodingParameters;

        /*
         * Update reconnecting UI
         */
        if (room != null) {
            reconnectingProgressBar.setVisibility(View.VISIBLE);
        }
        if(auto_answer) {

            connectToRoom(roomName);

        }
        else {
            answer.setOnClickListener(view -> connectToRoom(roomName));
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CallKitVoipPlugin instance = CallKitVoipPlugin.getInstance();
                    instance.notifyEvent("RejectCall",username,roomName);
                    CallActivity.this.stopService(new Intent(CallActivity.this, VoipForegroundService.class));
                    Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    CallActivity.this.sendBroadcast(it);

                    finish();



                }
            });
            callerUser.setVisibility(View.VISIBLE);
            callerUser.setText(username);
            answered_actions.setVisibility(View.GONE);
            unanswered_actions.setVisibility(View.VISIBLE);
        }

    }



    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
            /*
             * The first two permissions are Camera & Microphone, bluetooth isn't required but
             * enabling it enables bluetooth audio routing functionality.
             */
            boolean cameraAndMicPermissionGranted =
                    (PackageManager.PERMISSION_GRANTED == grantResults[CAMERA_PERMISSION_INDEX])
                            & (PackageManager.PERMISSION_GRANTED
                            == grantResults[MIC_PERMISSION_INDEX]);

            /*
             * Due to bluetooth permissions being requested at the same time as camera and mic
             * permissions, AudioSwitch should be started after providing the user the option
             * to grant the necessary permissions for bluetooth.
             */
            audioSwitch.start(
                    (audioDevices, audioDevice) -> Unit.INSTANCE);

            if (cameraAndMicPermissionGranted) {
                createAudioAndVideoTracks();
            } else {
                Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onPause() {
        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        if (localVideoTrack != null) {
            /*
             * If this local video track is being shared in a Room, unpublish from room before
             * releasing the video track. Participants will be notified that the track has been
             * unpublished.
             */
            if (localParticipant != null) {
                localParticipant.unpublishTrack(localVideoTrack);
            }

            localVideoTrack.release();
            localVideoTrack = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        /*
         * Tear down audio management and restore previous volume stream
         */
        audioSwitch.stop();
        setVolumeControlStream(savedVolumeControlStream);

        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room.getState() != Room.State.DISCONNECTED) {
            room.disconnect();
            disconnectedFromOnDestroy = true;
        }

        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
            localVideoTrack = null;
        }

        super.onDestroy();
    }

    private boolean checkPermissions(String[] permissions) {
        boolean shouldCheck = true;
        for (String permission : permissions) {
            shouldCheck &=
                    (PackageManager.PERMISSION_GRANTED
                            == ContextCompat.checkSelfPermission(this, permission));
        }
        return shouldCheck;
    }

    private void requestPermissions(String[] permissions) {
        boolean displayRational = false;
        for (String permission : permissions) {
            displayRational |=
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }
        if (displayRational) {
            Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this, permissions, CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermissionForCameraAndMicrophone() {
        return checkPermissions(
                new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
    }

    private void requestPermissionForCameraMicrophoneAndBluetooth() {
        String[] permissionsList;
            permissionsList =
                    new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        requestPermissions(permissionsList);
    }

    private void createAudioAndVideoTracks() {
        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(this, true, LOCAL_AUDIO_TRACK_NAME);
        // Share your camera
        cameraCapturerCompat =
                new CameraCapturerCompat(this, CameraCapturerCompat.Source.FRONT_CAMERA);
        localVideoTrack =
                LocalVideoTrack.create(this, true, cameraCapturerCompat, LOCAL_VIDEO_TRACK_NAME);
        primaryVideoView.setMirror(true);
        localVideoTrack.addSink(primaryVideoView);
        localVideoView = primaryVideoView;
    }


    private void connectToRoom(String roomName) {
        answered_actions.setVisibility(View.VISIBLE);
        unanswered_actions.setVisibility(View.GONE);
        callerUser.setVisibility(View.GONE);
        bcg.setVisibility(View.GONE);

        Intent serviceIntent = new Intent(this, VoipForegroundService.class);
        serviceIntent.setAction("answered");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CallActivity.this.startForegroundService(serviceIntent);

        } else {
            CallActivity.this.startService(serviceIntent);
        }
        audioSwitch.activate();
        ConnectOptions.Builder connectOptionsBuilder =
                new ConnectOptions.Builder(accessToken).roomName(roomName);

        /*
         * Add local audio track to connect options to share with participants.
         */
        if (localAudioTrack != null) {
            connectOptionsBuilder.audioTracks(Collections.singletonList(localAudioTrack));
        }

        /*
         * Add local video track to connect options to share with participants.
         */
        if (localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
        }

        /*
         * Set the preferred audio and video codec for media.
         */
        connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(audioCodec));
        connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(videoCodec));

        /*
         * Set the sender side encoding parameters.
         */
        Log.d("connectToRoom",connectOptionsBuilder.toString());
        Log.d("connectToRoom",encodingParameters.toString());

        connectOptionsBuilder.encodingParameters(encodingParameters);

        /*
         * Toggles automatic track subscription. If set to false, the LocalParticipant will receive
         * notifications of track publish events, but will not automatically subscribe to them. If
         * set to true, the LocalParticipant will automatically subscribe to tracks as they are
         * published. If unset, the default is true. Note: This feature is only available for Group
         * Rooms. Toggling the flag in a P2P room does not modify subscription behavior.
         */
        connectOptionsBuilder.enableAutomaticSubscription(enableAutomaticSubscription);

        room = Video.connect(this, connectOptionsBuilder.build(), roomListener());
        setDisconnectAction();
    }

    /*
     * The initial state when there is no active room.
     */
    private void intializeUI() {

        switchCameraActionFab.show();
        switchCameraActionFab.setOnClickListener(switchCameraClickListener());
        localVideoActionFab.show();
        localVideoActionFab.setOnClickListener(localVideoClickListener());
        muteActionFab.show();
        muteActionFab.setOnClickListener(muteClickListener());
    }



    /*
     * Get the preferred audio codec from shared preferences
     */
    private AudioCodec getAudioCodecPreference(String key, String defaultValue) {
        final String audioCodecName = preferences.getString(key, defaultValue);

        switch (audioCodecName) {
            case IsacCodec.NAME:
                return new IsacCodec();
            case OpusCodec.NAME:
                return new OpusCodec();
            case PcmaCodec.NAME:
                return new PcmaCodec();
            case PcmuCodec.NAME:
                return new PcmuCodec();
            case G722Codec.NAME:
                return new G722Codec();
            default:
                return new OpusCodec();
        }
    }

    /*
     * Get the preferred video codec from shared preferences
     */
    private VideoCodec getVideoCodecPreference(String key, String defaultValue) {
        final String videoCodecName = preferences.getString(key, defaultValue);

        switch (videoCodecName) {
            case Vp8Codec.NAME:
                boolean simulcast =
                        preferences.getBoolean(
                                SettingsActivity.PREF_VP8_SIMULCAST,
                                SettingsActivity.PREF_VP8_SIMULCAST_DEFAULT);
                return new Vp8Codec(simulcast);
            case H264Codec.NAME:
                return new H264Codec();
            case Vp9Codec.NAME:
                return new Vp9Codec();
            default:
                return new Vp8Codec();
        }
    }

    private boolean getAutomaticSubscriptionPreference(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private EncodingParameters getEncodingParameters() {
        final int maxAudioBitrate =
                Integer.parseInt(
                        preferences.getString(
                                SettingsActivity.PREF_SENDER_MAX_AUDIO_BITRATE,
                                SettingsActivity.PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT));
        final int maxVideoBitrate =
                Integer.parseInt(
                        preferences.getString(
                                SettingsActivity.PREF_SENDER_MAX_VIDEO_BITRATE,
                                SettingsActivity.PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT));

        return new EncodingParameters(maxAudioBitrate, maxVideoBitrate);
    }

    /*
     * The actions performed during disconnect.
     */
    private void setDisconnectAction() {
        connectActionFab.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_hangup));
        connectActionFab.show();
        connectActionFab.setOnClickListener(disconnectClickListener());
    }

    /*
     * Called when remote participant joins the room
     */
    @SuppressLint("SetTextI18n")
    private void addRemoteParticipant(RemoteParticipant remoteParticipant) {
        /*
         * This app only displays video for one additional participant per Room
         */
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            Snackbar.make(
                    connectActionFab,
                    "Multiple participants are not currently support in this UI",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
            return;
        }
        remoteParticipantIdentity = remoteParticipant.getIdentity();

        /*
         * Add remote participant renderer
         */
        if (remoteParticipant.getRemoteVideoTracks().size() > 0) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    remoteParticipant.getRemoteVideoTracks().get(0);

            /*
             * Only render video tracks that are subscribed to
             */
            if (remoteVideoTrackPublication.isTrackSubscribed()) {
                addRemoteParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }

        /*
         * Start listening for participant events
         */
        remoteParticipant.setListener(remoteParticipantListener());
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private void addRemoteParticipantVideo(VideoTrack videoTrack) {
        moveLocalVideoToThumbnailView();
        primaryVideoView.setMirror(false);
        videoTrack.addSink(primaryVideoView);
    }
    public static int dpToPx(int dp)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
    private void switch_views()
    {
        Log.d("switch_views","called");
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            if (thumb_for_local)
            {
                thumb_for_local = false;
                ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams)primaryVideoView.getLayoutParams();
                ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams)thumbnailVideoView.getLayoutParams();

                params1.height = dpToPx(96);
                params1.width = dpToPx(96);
                params2.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params2.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params2.leftMargin = 0;
                params2.rightMargin = 0;
                params2.topMargin = 0;
                params2.bottomMargin = 0;
                params1.leftMargin = dpToPx(16);
                params1.rightMargin = dpToPx(16);
                params1.topMargin = dpToPx(16);
                params1.bottomMargin = dpToPx(16);

                primaryVideoView.setLayoutParams(params1);
                primaryVideoView.setMirror(true);
                primaryVideoView.setZOrderMediaOverlay(true);
                thumbnailVideoView.setLayoutParams(params2);
                thumbnailVideoView.setMirror(false);
                thumbnailVideoView.setZOrderMediaOverlay(false);
            }
            else {
                thumb_for_local = true;

                ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams)primaryVideoView.getLayoutParams();
                ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams)thumbnailVideoView.getLayoutParams();

                params1.height = dpToPx(96);
                params1.width = dpToPx(96);
                params2.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params2.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params2.leftMargin = 0;
                params2.rightMargin = 0;
                params2.topMargin = 0;
                params2.bottomMargin = 0;
                params1.leftMargin = dpToPx(16);
                params1.rightMargin = dpToPx(16);
                params1.topMargin = dpToPx(16);
                params1.bottomMargin = dpToPx(16);

                primaryVideoView.setLayoutParams(params2);
                primaryVideoView.setMirror(false);
                primaryVideoView.setZOrderMediaOverlay(false);
                thumbnailVideoView.setLayoutParams(params1);
                thumbnailVideoView.setMirror(true);
                thumbnailVideoView.setZOrderMediaOverlay(true);
            }

        }
    }
    private void moveLocalVideoToThumbnailView() {
        if (thumbnailVideoView.getVisibility() == View.GONE) {
            try {
                thumbnailVideoView.setVisibility(View.VISIBLE);
                localVideoTrack.removeSink(primaryVideoView);
                localVideoTrack.addSink(thumbnailVideoView);
                localVideoView = thumbnailVideoView;
                thumbnailVideoView.setMirror(
                        cameraCapturerCompat.getCameraSource()
                                == CameraCapturerCompat.Source.FRONT_CAMERA);
            }
            catch (Exception e)
            {

            }
        }
    }

    /*
     * Called when remote participant leaves the room
     */
    @SuppressLint("SetTextI18n")
    private void removeRemoteParticipant(RemoteParticipant remoteParticipant) {
        if (!remoteParticipant.getIdentity().equals(remoteParticipantIdentity)) {
            return;
        }

        /*
         * Remove remote participant renderer
         */
        if (!remoteParticipant.getRemoteVideoTracks().isEmpty()) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    remoteParticipant.getRemoteVideoTracks().get(0);

            /*
             * Remove video only if subscribed to participant track
             */
            if (remoteVideoTrackPublication.isTrackSubscribed()) {
                removeParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }
        moveLocalVideoToPrimaryView();
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        videoTrack.removeSink(primaryVideoView);
    }

    private void moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            thumbnailVideoView.setVisibility(View.GONE);
            if (localVideoTrack != null) {
                localVideoTrack.removeSink(thumbnailVideoView);
                localVideoTrack.addSink(primaryVideoView);
            }
            localVideoView = primaryVideoView;
            primaryVideoView.setMirror(
                    cameraCapturerCompat.getCameraSource()
                            == CameraCapturerCompat.Source.FRONT_CAMERA);
        }
    }

    /*
     * Room events listener
     */
    @SuppressLint("SetTextI18n")
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                localParticipant = room.getLocalParticipant();
                setTitle(room.getName());
                reconnectingProgressBar.setVisibility(View.GONE);

                for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                    addRemoteParticipant(remoteParticipant);
                    break;
                }
            }

            @Override
            public void onReconnecting(
                    @NonNull Room room, @NonNull TwilioException twilioException) {
                reconnectingProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReconnected(@NonNull Room room) {
                reconnectingProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                audioSwitch.deactivate();
                intializeUI();
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                localParticipant = null;
                reconnectingProgressBar.setVisibility(View.GONE);
                CallActivity.this.room = null;
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    audioSwitch.deactivate();
                    intializeUI();
                    moveLocalVideoToPrimaryView();
                }
                finish();
            }

            @Override
            public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
                addRemoteParticipant(remoteParticipant);
            }

            @Override
            public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {
                finish();
                removeRemoteParticipant(remoteParticipant);
            }

            @Override
            public void onRecordingStarted(Room room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(Room room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStopped");
            }
        };
    }

    @SuppressLint("SetTextI18n")
    private RemoteParticipant.Listener remoteParticipantListener() {
        return new RemoteParticipant.Listener() {
            @Override
            public void onAudioTrackPublished(
                    RemoteParticipant remoteParticipant,
                    RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(
                        TAG,
                        String.format(
                                "onAudioTrackPublished: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteAudioTrackPublication: sid=%s, enabled=%b, "
                                        + "subscribed=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteAudioTrackPublication.getTrackSid(),
                                remoteAudioTrackPublication.isTrackEnabled(),
                                remoteAudioTrackPublication.isTrackSubscribed(),
                                remoteAudioTrackPublication.getTrackName()));
            }

            @Override
            public void onAudioTrackUnpublished(
                    RemoteParticipant remoteParticipant,
                    RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(
                        TAG,
                        String.format(
                                "onAudioTrackUnpublished: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteAudioTrackPublication: sid=%s, enabled=%b, "
                                        + "subscribed=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteAudioTrackPublication.getTrackSid(),
                                remoteAudioTrackPublication.isTrackEnabled(),
                                remoteAudioTrackPublication.isTrackSubscribed(),
                                remoteAudioTrackPublication.getTrackName()));
            }

            @Override
            public void onDataTrackPublished(
                    RemoteParticipant remoteParticipant,
                    RemoteDataTrackPublication remoteDataTrackPublication) {
                Log.i(
                        TAG,
                        String.format(
                                "onDataTrackPublished: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteDataTrackPublication: sid=%s, enabled=%b, "
                                        + "subscribed=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteDataTrackPublication.getTrackSid(),
                                remoteDataTrackPublication.isTrackEnabled(),
                                remoteDataTrackPublication.isTrackSubscribed(),
                                remoteDataTrackPublication.getTrackName()));
            }

            @Override
            public void onDataTrackUnpublished(
                    RemoteParticipant remoteParticipant,
                    RemoteDataTrackPublication remoteDataTrackPublication) {
                Log.i(
                        TAG,
                        String.format(
                                "onDataTrackUnpublished: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteDataTrackPublication: sid=%s, enabled=%b, "
                                        + "subscribed=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteDataTrackPublication.getTrackSid(),
                                remoteDataTrackPublication.isTrackEnabled(),
                                remoteDataTrackPublication.isTrackSubscribed(),
                                remoteDataTrackPublication.getTrackName()));
            }

            @Override
            public void onVideoTrackPublished(
                    RemoteParticipant remoteParticipant,
                    RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(
                        TAG,
                        String.format(
                                "onVideoTrackPublished: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteVideoTrackPublication: sid=%s, enabled=%b, "
                                        + "subscribed=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteVideoTrackPublication.getTrackSid(),
                                remoteVideoTrackPublication.isTrackEnabled(),
                                remoteVideoTrackPublication.isTrackSubscribed(),
                                remoteVideoTrackPublication.getTrackName()));
            }

            @Override
            public void onVideoTrackUnpublished(
                    RemoteParticipant remoteParticipant,
                    RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(
                        TAG,
                        String.format(
                                "onVideoTrackUnpublished: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteVideoTrackPublication: sid=%s, enabled=%b, "
                                        + "subscribed=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteVideoTrackPublication.getTrackSid(),
                                remoteVideoTrackPublication.isTrackEnabled(),
                                remoteVideoTrackPublication.isTrackSubscribed(),
                                remoteVideoTrackPublication.getTrackName()));
            }

            @Override
            public void onAudioTrackSubscribed(
                    RemoteParticipant remoteParticipant,
                    RemoteAudioTrackPublication remoteAudioTrackPublication,
                    RemoteAudioTrack remoteAudioTrack) {
                Log.i(
                        TAG,
                        String.format(
                                "onAudioTrackSubscribed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteAudioTrack.isEnabled(),
                                remoteAudioTrack.isPlaybackEnabled(),
                                remoteAudioTrack.getName()));
            }

            @Override
            public void onAudioTrackUnsubscribed(
                    RemoteParticipant remoteParticipant,
                    RemoteAudioTrackPublication remoteAudioTrackPublication,
                    RemoteAudioTrack remoteAudioTrack) {
                Log.i(
                        TAG,
                        String.format(
                                "onAudioTrackUnsubscribed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteAudioTrack.isEnabled(),
                                remoteAudioTrack.isPlaybackEnabled(),
                                remoteAudioTrack.getName()));
            }

            @Override
            public void onAudioTrackSubscriptionFailed(
                    RemoteParticipant remoteParticipant,
                    RemoteAudioTrackPublication remoteAudioTrackPublication,
                    TwilioException twilioException) {
                Log.i(
                        TAG,
                        String.format(
                                "onAudioTrackSubscriptionFailed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteAudioTrackPublication: sid=%b, name=%s]"
                                        + "[TwilioException: code=%d, message=%s]",
                                remoteParticipant.getIdentity(),
                                remoteAudioTrackPublication.getTrackSid(),
                                remoteAudioTrackPublication.getTrackName(),
                                twilioException.getCode(),
                                twilioException.getMessage()));
            }

            @Override
            public void onDataTrackSubscribed(
                    RemoteParticipant remoteParticipant,
                    RemoteDataTrackPublication remoteDataTrackPublication,
                    RemoteDataTrack remoteDataTrack) {
                Log.i(
                        TAG,
                        String.format(
                                "onDataTrackSubscribed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteDataTrack: enabled=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteDataTrack.isEnabled(),
                                remoteDataTrack.getName()));
            }

            @Override
            public void onDataTrackUnsubscribed(
                    RemoteParticipant remoteParticipant,
                    RemoteDataTrackPublication remoteDataTrackPublication,
                    RemoteDataTrack remoteDataTrack) {
                Log.i(
                        TAG,
                        String.format(
                                "onDataTrackUnsubscribed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteDataTrack: enabled=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteDataTrack.isEnabled(),
                                remoteDataTrack.getName()));
            }

            @Override
            public void onDataTrackSubscriptionFailed(
                    RemoteParticipant remoteParticipant,
                    RemoteDataTrackPublication remoteDataTrackPublication,
                    TwilioException twilioException) {
                Log.i(
                        TAG,
                        String.format(
                                "onDataTrackSubscriptionFailed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteDataTrackPublication: sid=%b, name=%s]"
                                        + "[TwilioException: code=%d, message=%s]",
                                remoteParticipant.getIdentity(),
                                remoteDataTrackPublication.getTrackSid(),
                                remoteDataTrackPublication.getTrackName(),
                                twilioException.getCode(),
                                twilioException.getMessage()));
            }

            @Override
            public void onVideoTrackSubscribed(
                    RemoteParticipant remoteParticipant,
                    RemoteVideoTrackPublication remoteVideoTrackPublication,
                    RemoteVideoTrack remoteVideoTrack) {
                Log.i(
                        TAG,
                        String.format(
                                "onVideoTrackSubscribed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteVideoTrack: enabled=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteVideoTrack.isEnabled(),
                                remoteVideoTrack.getName()));
                addRemoteParticipantVideo(remoteVideoTrack);
            }

            @Override
            public void onVideoTrackUnsubscribed(
                    RemoteParticipant remoteParticipant,
                    RemoteVideoTrackPublication remoteVideoTrackPublication,
                    RemoteVideoTrack remoteVideoTrack) {
                Log.i(
                        TAG,
                        String.format(
                                "onVideoTrackUnsubscribed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteVideoTrack: enabled=%b, name=%s]",
                                remoteParticipant.getIdentity(),
                                remoteVideoTrack.isEnabled(),
                                remoteVideoTrack.getName()));
                removeParticipantVideo(remoteVideoTrack);
            }

            @Override
            public void onVideoTrackSubscriptionFailed(
                    RemoteParticipant remoteParticipant,
                    RemoteVideoTrackPublication remoteVideoTrackPublication,
                    TwilioException twilioException) {
                Log.i(
                        TAG,
                        String.format(
                                "onVideoTrackSubscriptionFailed: "
                                        + "[RemoteParticipant: identity=%s], "
                                        + "[RemoteVideoTrackPublication: sid=%b, name=%s]"
                                        + "[TwilioException: code=%d, message=%s]",
                                remoteParticipant.getIdentity(),
                                remoteVideoTrackPublication.getTrackSid(),
                                remoteVideoTrackPublication.getTrackName(),
                                twilioException.getCode(),
                                twilioException.getMessage()));
                Snackbar.make(
                        connectActionFab,
                        String.format(
                                "Failed to subscribe to %s video track",
                                remoteParticipant.getIdentity()),
                        Snackbar.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onAudioTrackEnabled(
                    RemoteParticipant remoteParticipant,
                    RemoteAudioTrackPublication remoteAudioTrackPublication) {}

            @Override
            public void onAudioTrackDisabled(
                    RemoteParticipant remoteParticipant,
                    RemoteAudioTrackPublication remoteAudioTrackPublication) {}

            @Override
            public void onVideoTrackEnabled(
                    RemoteParticipant remoteParticipant,
                    RemoteVideoTrackPublication remoteVideoTrackPublication) {}

            @Override
            public void onVideoTrackDisabled(
                    RemoteParticipant remoteParticipant,
                    RemoteVideoTrackPublication remoteVideoTrackPublication) {}
        };
    }


    private View.OnClickListener disconnectClickListener() {
        return v -> {
            /*
             * Disconnect from room
             */
            if (room != null) {
                room.disconnect();
            }
            intializeUI();
        };
    }



    private View.OnClickListener switchCameraClickListener() {
        return v -> {
            if (cameraCapturerCompat != null) {
                CameraCapturerCompat.Source cameraSource = cameraCapturerCompat.getCameraSource();
                cameraCapturerCompat.switchCamera();
                if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
                    thumbnailVideoView.setMirror(
                            cameraSource == CameraCapturerCompat.Source.BACK_CAMERA);
                } else {
                    primaryVideoView.setMirror(
                            cameraSource == CameraCapturerCompat.Source.BACK_CAMERA);
                }
            }
        };
    }

    private View.OnClickListener localVideoClickListener() {
        return v -> {
            /*
             * Enable/disable the local video track
             */
            if (localVideoTrack != null) {
                boolean enable = !localVideoTrack.isEnabled();
                localVideoTrack.enable(enable);
                int icon;
                if (enable) {
                    icon = R.drawable.ic_disable_camera;
                    switchCameraActionFab.show();
                } else {
                    icon = R.drawable.ic_enable_camera;
                    switchCameraActionFab.hide();
                }
                localVideoActionFab.setImageDrawable(
                        ContextCompat.getDrawable(CallActivity.this, icon));
            }
        };
    }

    private View.OnClickListener muteClickListener() {
        return v -> {
            /*
             * Enable/disable the local audio track. The results of this operation are
             * signaled to other Participants in the same Room. When an audio track is
             * disabled, the audio is muted.
             */
            if (localAudioTrack != null) {
                boolean enable = !localAudioTrack.isEnabled();
                localAudioTrack.enable(enable);
                int icon = enable ? R.drawable.ic_mic : R.drawable.ic_mic_disabled;
                muteActionFab.setImageDrawable(ContextCompat.getDrawable(CallActivity.this, icon));
            }
        };
    }


}
