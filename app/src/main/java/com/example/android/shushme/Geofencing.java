package com.example.android.shushme;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Geofencing implements ResultCallback<Status> {

    public static final String TAG = Geofencing.class.getSimpleName();

    private static final float GEOFENCE_DEFAULT_RADIUS = 75;
    private static final long GEOFENCE_DEFAULT_DURATION = 86400000; // ms
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 117;
    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mGeofenceList;

    public Geofencing(Context context, GoogleApiClient googleApiClient) {
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
    }


    /**
     * REGISTERALLGEOFENCES - Registers the list of Geofences in the Geofence list with Google Play Services
     */
    public void registerAllGeofences() {

        // Check that the list is not empty and that we are connected to API Client
        if ((mGoogleApiClient == null) || (!mGoogleApiClient.isConnected())
                || (mGeofenceList == null) || (mGeofenceList.size() == 0)) {
            return;
        }

        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                ActivityCompat.requestPermissions((Activity) mContext,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCATION);

                return;
            }
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException se) {
            Log.e(TAG, "registerAllGeofences: ", new UnsupportedOperationException("No security"));
        }

    }


    /**
     * UNREGISTERALLGEOFENCES - Simpler because it does not require a geofencing request.
     * Unregisters all the geofences that were registered before from Google Play Services
     */
    public void unregisterAllGeofences() {
        if ((mGoogleApiClient == null) || (! mGoogleApiClient.isConnected())) {
            return;
        }

        try {
            LocationServices.GeofencingApi
                    .removeGeofences(mGoogleApiClient, getGeofencePendingIntent())  // Same PendingIntent as before
                    .setResultCallback(this);
        } catch (SecurityException e) {
            // May be created by not having ACCESS FINE PERMISSION
            Log.e(TAG, e.getMessage() );
        }
    }


    /**
     * UPDATEGEOFENCESLIST - given a PlaceBuffer will create a Geofence object for each Place using Geofence.Builder
     * and add that Geofence to mGeofenceList
     * @param aPlaceBuffer - holds Places
     */
    public void updateGeofencesList(PlaceBuffer aPlaceBuffer) {


        mGeofenceList = new ArrayList<>();
        // If PlaceBuffer is not ready, neither are we
        if ((aPlaceBuffer == null) || (aPlaceBuffer.getCount() == 0)) {
            return;
        }

        Geofence newGeofence;
        for (Place place : aPlaceBuffer) {
            LatLng latLng = place.getLatLng();
            double latitude = latLng.latitude;
            double longitude = latLng.longitude;


            newGeofence = new Geofence.Builder()
                    .setRequestId(place.getId())
                    .setCircularRegion(
                            latitude,
                            longitude,
                            GEOFENCE_DEFAULT_RADIUS
                    )
                    .setExpirationDuration(GEOFENCE_DEFAULT_DURATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofenceList.add(newGeofence);
        }

        return;

    }

    /**
     * GETGEOFENCINGREQUEST - Makes request for all geofences in the geofence list.
     *  a private helper method called getGeofencingRequest that
     *  uses GeofencingRequest.Builder to return a GeofencingRequest object from the Geofence list
     */
    private GeofencingRequest getGeofencingRequest() {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        return builder
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(mGeofenceList)
                .build();

    }


    /**
     * GETGEOFENCEPENDINGINTENT - Will create our PendingIntent for us.
     * @return - Created or previous PendingIntent
     */
    public PendingIntent getGeofencePendingIntent() {
        // Reuse if already have one
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class) ;
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }



    @Override
    public void onResult(@NonNull Status status) {
        Log.e(TAG, String.format("There was an error adding/removing geofence : %s",
                status.toString()) );
    }
}
