package com.example.android.sunshine.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class WearableDataListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {
    public static final String WEATHER_DATA_PATH = "/weather-data";
    public static final String IMAGE_ASSET_KEY = "image-asset";
    public static final String HIGH_TEMP_KEY = "temp-high";
    public static final String LOW_TEMP_KEY = "temp-low";
    public static final String DESC_KEY = "weather-desc";
    private static final String LOG_TAG = WearableDataListenerService.class.getSimpleName();
    GoogleApiClient mGoogleApiClient;

    public WearableDataListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);

        Utility.logD(LOG_TAG, "onDataChanged");

        String weatherDesc;
        double high, low;
        Asset iconAsset;



        for (DataEvent dataEvent : dataEventBuffer) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();


                if (path.equals(WEATHER_DATA_PATH)) {
//                    mHigh = dataMap.getDouble(HIGH_TEMP_KEY);
//                    low = dataMap.getDouble(LOW_TEMP_KEY);
//                    weatherDesc = dataMap.getString(DESC_KEY);
//                    iconAsset = dataMap.getAsset(IMAGE_ASSET_KEY);

                    Utility.logD(LOG_TAG, "Test DataItem updated: " + dataMap.getString(DESC_KEY));

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean(SunshineWatchFaceService.PREF_DATA_CHANGED_KEY, true);
                    prefsEditor.apply();

                }

            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
