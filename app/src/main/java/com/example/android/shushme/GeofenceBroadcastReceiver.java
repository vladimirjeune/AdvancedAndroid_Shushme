package com.example.android.shushme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Remember to add a BroadcastReceiver tag to the manifest
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    /**
     * ONRECEIVE - Broadcast sent when the Geofence Transition message is sent
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Remember that this is on the main Thread. You should make an Asynchronous Task for
        // anything that runs longer than a few seconds.  No ANR.

        Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

    }

}
