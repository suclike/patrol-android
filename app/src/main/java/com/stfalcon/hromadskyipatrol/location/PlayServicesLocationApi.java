/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by alex on 02.10.15.
 */
public class PlayServicesLocationApi implements LocationApi, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOCATION_TAG = PlayServicesLocationApi.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Location prev_location;
    private LocationRequest locationRequest;
    private MyLocationListener callback;

    @Override
    public void init(Context context) {
        buildGoogleApiClient(context);
    }

    @Override
    public boolean isWorked() {
        return mGoogleApiClient.isConnected();
    }

    @Override
    public void createLocationRequest(int type) {
        switch (type){
            case LocationApi.LOW_ACCURACY_LOCATION:
                locationRequest = new LocationRequest();
                locationRequest.setInterval(30 * 1000); // 30 sec
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case LocationApi.HIGH_ACCURACY_LOCATION:
                locationRequest = new LocationRequest();
                locationRequest.setInterval(2000);  // 2 sec
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
        }
    }


    @Override
    public void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this);
        } catch (IllegalStateException e) {
            mGoogleApiClient.connect();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startLocationUpdates();
                }
            }, 10000);  // Try after 10sec
        }
    }

    @Override
    public void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        } catch (IllegalStateException e) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopLocationUpdates();
                }
            }, 10000);  // Try after 10sec
        }
    }

    @Override
    public Location getPreviousLocation() {
        return prev_location;
    }


    @Override
    public void setLocationListener(MyLocationListener listener) {
        callback = listener;
    }


    protected synchronized void buildGoogleApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        mGoogleApiClient.connect();
    }




    @Override
    public void onConnected(Bundle bundle) {
        prev_location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            Crashlytics.log(Log.ERROR, LOCATION_TAG,
                    "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        prev_location = location;
        if (callback != null){
            callback.onLocationChanged(location);
        }
    }
}
