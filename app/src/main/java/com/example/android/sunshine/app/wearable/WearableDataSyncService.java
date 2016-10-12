package com.example.android.sunshine.app.wearable;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WearableDataSyncService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    public static final String WEATHER_DATA_PATH = "/weather-data";
    public static final String IMAGE_ASSET_KEY = "image-asset";
    public static final String HIGH_TEMP_KEY = "temp-high";
    public static final String LOW_TEMP_KEY = "temp-low";
    public static final String DESC_KEY = "weather-desc";
    private static final String LOG_TAG = WearableDataSyncService.class.getSimpleName();
    private static final String EXTRA_ICON_ID = "EXTRA_ICON_ID";
    private static final String EXTRA_LOW = "EXTRA_LOW";
    private static final String EXTRA_HIGH = "EXTRA_HIGH";
    private static final String EXTRA_DESC = "EXTRA_DESC";
    public GoogleApiClient mGoogleApiClient;

    public WearableDataSyncService() {
        super("WearableDataSyncService");
    }

    /**
     * Starts this service to perform action to sync DataItems with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void syncData(Context context, int iconId, double high, double low, String desc) {
        Intent intent = new Intent(context, WearableDataSyncService.class);
        intent.putExtra(EXTRA_ICON_ID, iconId);
        intent.putExtra(EXTRA_HIGH, high);
        intent.putExtra(EXTRA_LOW, low);
        intent.putExtra(EXTRA_DESC, desc);

        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int iconId = intent.getIntExtra(EXTRA_ICON_ID, R.drawable.ic_no_data);
            double high = intent.getDoubleExtra(EXTRA_HIGH, Double.NaN);
            double low = intent.getDoubleExtra(EXTRA_LOW, Double.NaN);
            String desc = intent.getStringExtra(EXTRA_DESC);
            if(desc != null){
                //SYNC DATA
                Bitmap weatherIconBitmap = BitmapFactory.decodeResource(getResources(), iconId);
                updateWatchFaceData(weatherIconBitmap, high, low, desc);
            }
            else {
                Log.d(LOG_TAG, "onHandleIntent: Test data null");
            }
        }
    }


    private void updateWatchFaceData(Bitmap weatherIconBitmap, double high, double low, String desc) {
        Log.d(LOG_TAG, "updateWatchFaceData: " + desc);

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(WEATHER_DATA_PATH);

        Asset weatherIconAsset = createAssetFromBitmap(weatherIconBitmap);
        putDataMapRequest.getDataMap().putAsset(IMAGE_ASSET_KEY, weatherIconAsset);

        putDataMapRequest.getDataMap().putDouble(HIGH_TEMP_KEY, high);
        putDataMapRequest.getDataMap().putDouble(LOW_TEMP_KEY, low);
        putDataMapRequest.getDataMap().putString(DESC_KEY, desc);

        putDataMapRequest.getDataMap().putLong("time-stamp", System.currentTimeMillis());

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        putDataRequest.setUrgent();

        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if (dataItemResult.getStatus().isSuccess()) {
                            // Success
                            Log.d(LOG_TAG, "updateWatchFaceData: Success");
                        } else {
                            // Boo
                            Log.d(LOG_TAG, "updateWatchFaceData: Failed");
                        }
                    }
                });

    }

    private Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
