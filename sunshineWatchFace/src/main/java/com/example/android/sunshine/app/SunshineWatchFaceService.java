package com.example.android.sunshine.app;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class SunshineWatchFaceService extends CanvasWatchFaceService {

    public static final String PREF_DATA_CHANGED_KEY = "data-changed";
    public static final String WEATHER_DATA_PATH = "/weather-data";
    public static final String IMAGE_ASSET_KEY = "image-asset";
    public static final String HIGH_TEMP_KEY = "temp-high";
    public static final String LOW_TEMP_KEY = "temp-low";
    public static final String DESC_KEY = "weather-desc";
    private static final String LOG_TAG = SunshineWatchFaceService.class.getSimpleName();
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);


    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    public class Engine extends CanvasWatchFaceService.Engine
            implements SharedPreferences.OnSharedPreferenceChangeListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {


        int mBgColor;

        Paint mClockBgPaint;
        Paint mHrPaint;
        Paint mColonPaint;
        Paint mMinPaint;
        Paint mAMPMPaint;

        Paint mWeatherBgPaint;
        Paint mMaxTempPaint;
        Paint mMinTempPaint;

        private GoogleApiClient mGoogleApiClient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            Utility.logD(LOG_TAG, "Registering preference changed listener");
            PreferenceManager.getDefaultSharedPreferences(SunshineWatchFaceService.this)
                    .registerOnSharedPreferenceChangeListener(this);

            mBgColor = getColor(R.color.black_86p);

            mClockBgPaint = new Paint();
            mClockBgPaint.setColor(getColor(R.color.black_86p));
            mClockBgPaint.setAntiAlias(true);
            mHrPaint = createTextPaint(getColor(R.color.white),
                    BOLD_TYPEFACE);
            mMinPaint = createTextPaint(getColor(R.color.white),
                    BOLD_TYPEFACE);
            mColonPaint = createTextPaint(getColor(R.color.grey_700),
                    BOLD_TYPEFACE);
            mAMPMPaint = createTextPaint(getColor(R.color.grey));

            mWeatherBgPaint = new Paint();
            mWeatherBgPaint.setAntiAlias(true);
            mWeatherBgPaint.setColor(getColor(R.color.blue));
            mMaxTempPaint = createTextPaint(getColor(R.color.white),
                    BOLD_TYPEFACE);
            mMinTempPaint = createTextPaint(getColor(R.color.grey),
                    BOLD_TYPEFACE);
            // configure the system UI
            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle
                            .BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());


            mGoogleApiClient = new GoogleApiClient.Builder(SunshineWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();


        }

        private Paint createTextPaint(int color) {
            return createTextPaint(color, NORMAL_TYPEFACE);
        }

        private Paint createTextPaint(int color, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(5.0f);

            int scaledSize = getResources().getDimensionPixelSize(R.dimen.text_size);
            paint.setTextSize(scaledSize);

            return paint;
        }


        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();

            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            //set paint colors
            //set anti alias
            //invalidate

        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            canvas.drawColor(mBgColor);

            drawClock(canvas, bounds);
            drawWeather(canvas, bounds);

        }

        private void drawClock(Canvas canvas, Rect bounds) {

            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches
            // with a "chin", the watch face is centered on the entire screen, not
            // just the usable portion.
            float centerX = width / 2f;
            float centerY = height / 2f;


            int xOffset = (int) getResources().getDimension(R.dimen.clock_pos_shift);
            int yOffset = (int) getResources().getDimension(R.dimen.clock_pos_shift);

//            int left, right, top, bottom;

            //Minutes arc
//            left = (int) centerX - width - xOffset;
//            top = (int) centerY - height - yOffset;
//            right = (int) centerX + width - xOffset;
//            bottom = (int)centerY + height - yOffset;
//            canvas.drawArc(left, top, right, bottom, -90, 160, true, mTest_backgroundPaint);

            //Clock circle and marks
//            canvas.drawCircle(centerX-xOffset, centerY-yOffset, width*0.45f, mClockBgPaint);

        }

        private void drawWeather(Canvas canvas, Rect bounds) {

            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches
            // with a "chin", the watch face is centered on the entire screen, not
            // just the usable portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            int xOffset = (int) getResources().getDimension(R.dimen.weather_pos_shift_rect);
            int yOffset = (int) getResources().getDimension(R.dimen.weather_pos_shift_rect);

            //Clock circle and marks
            canvas.drawCircle(centerX + xOffset, centerY + yOffset, width * 0.23f, mWeatherBgPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            PreferenceManager.getDefaultSharedPreferences(SunshineWatchFaceService.this)
                    .unregisterOnSharedPreferenceChangeListener(this);

            if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            //TODO use

            Utility.logD(LOG_TAG, "onSharedPreferenceChangedListener: " + s);

            if (s.equals(PREF_DATA_CHANGED_KEY) && sharedPreferences.getBoolean(PREF_DATA_CHANGED_KEY, false)) {

                //Resetting PREF_DATA_CHANGED
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SunshineWatchFaceService.this);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putBoolean(SunshineWatchFaceService.PREF_DATA_CHANGED_KEY, false);
                prefsEditor.apply();


                if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                    Utility.logD(LOG_TAG, "onSharedPreferenceChangedListener: mGoogleApiClient is not connected.");
                    return;
                }


                Utility.logD(LOG_TAG, "onSharedPreferenceChangedListener: mGoogleApiClient is connected.");

                Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {

                        Utility.logD(LOG_TAG, "getLocalNode, ResultCallback, onResult called ");

                        Uri uri = new Uri.Builder()
                                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                                .path(WEATHER_DATA_PATH)
                                .build();

                        Wearable.DataApi.getDataItems(mGoogleApiClient, uri)
                                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                                    @Override
                                    public void onResult(@NonNull DataItemBuffer dataItems) {
                                        for (DataItem dataItem : dataItems) {

                                            Utility.logD(LOG_TAG, "Data item path: " + dataItem.getUri().getPath());

                                            if (dataItem.getUri().getPath().equals(WEATHER_DATA_PATH)) {
                                                DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();

                                                double high = dataMap.getDouble(HIGH_TEMP_KEY);
                                                double low = dataMap.getDouble(LOW_TEMP_KEY);
                                                String weatherDesc = dataMap.getString(DESC_KEY);
                                                Asset iconAsset = dataMap.getAsset(IMAGE_ASSET_KEY);

                                                Utility.logD(LOG_TAG, "Got data, desc: " + weatherDesc);

                                            }
                                        }

                                        dataItems.release();
                                    }
                                });

                    }
                });
            }

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


}
