package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();
    public static final int NOTIFICATION_ID = 137;

    /***
     * Handles the Broadcast message sent when the Geofence Transition is triggered
     * Careful here though, this is running on the main thread so make sure you start an AsyncTask for
     * anything that takes longer than say 10 second to run
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive called");
        // TODO (4) Use GeofencingEvent.fromIntent to retrieve the GeofencingEvent that caused the transition
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);



        // TODO (5) Call getGeofenceTransition to get the transition type and use AudioManager to set the
        // phone ringer mode based on the transition type. Feel free to create a helper method (setRingerMode)
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                setRingerMode(context, AudioManager.RINGER_MODE_NORMAL);
                break;

            default:
                Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition) );  // Should say as string
                return;
        }


        // TODO (6) Show a notification to alert the user that the ringer mode has changed.
        // Feel free to create a helper method (sendNotification)
        sendNotification(context, geofenceTransition);

    }


    /**
     * SENDNOTIFICATION - Will send notification that we have entered/exited a Geofence on our list.
     * @param context
     * @param geofenceTransitionType
     */
    private void sendNotification(Context context, int geofenceTransitionType) {
        // Check transition type to get the correct image
        NotificationCompat.Builder notificationBuilder = new NotificationCompat
                .Builder(context, MainActivity.CHANNEL_ID);

        switch (geofenceTransitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                notificationBuilder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                        .setContentTitle(context.getString(R.string.notify_geofence_transition))
                        .setContentText(context.getString(R.string.notify_entering_geofence))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                notificationBuilder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                        .setContentTitle(context.getString(R.string.notify_geofence_transition))
                        .setContentText(context.getString(R.string.notify_exiting_geofence))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                break;

            default:
                Log.e(TAG, String.format("Unknown transition type: %d", geofenceTransitionType) );
        }

        // Notify
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }


    /**
     * SETRINGERMODE - Sets what mode the ringer will be in
     * @param context - Needed for some functions calls, and AUDIO_SERVICE
     * @param mode - What mode we should be in
     */
    public void setRingerMode(Context context, int mode) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // If > N and Permissions are not granted, or we are less than N and Permissions therefore are
        // not needed.
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && ! notificationManager.isNotificationPolicyAccessGranted())) {

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager != null) {
                audioManager.setRingerMode(mode);
            }
        }


    }

}
