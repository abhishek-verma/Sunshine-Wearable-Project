package com.example.android.sunshine.app;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Log;
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

import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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
    private static final long TIMEOUT_MS = 100;


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
        Paint mWeatherOuterCirclePaint;
        Paint mMaxTempPaint;
        Paint mMinTempPaint;
        Paint mDescPaint;
        double mHigh = Double.NaN;
        double mLow = Double.NaN;
        String mWeatherDesc = null;
        Asset mIconAsset = null;
        Bitmap mIconBitmap = null;
        private Calendar mCalendar;
        private GoogleApiClient mGoogleApiClient;

        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            //Resetting PREF_DATA_CHANGED
            PreferenceManager
                    .getDefaultSharedPreferences(SunshineWatchFaceService.this)
                    .edit()
                    .putBoolean(SunshineWatchFaceService.PREF_DATA_CHANGED_KEY, false)
                    .apply();

            Utility.logD(LOG_TAG, "Registering preference changed listener");
            PreferenceManager.getDefaultSharedPreferences(SunshineWatchFaceService.this)
                    .registerOnSharedPreferenceChangeListener(this);

            mBgColor = getColor(R.color.black_86p);

            mClockBgPaint = new Paint();
            mClockBgPaint.setColor(getColor(R.color.black_86p));
            mClockBgPaint.setAntiAlias(true);
            mHrPaint = createTextPaint(getColor(R.color.white),
                    BOLD_TYPEFACE, (int) getResources().getDimension(R.dimen.text_size_large));
            mMinPaint = createTextPaint(getColor(R.color.white),
                    BOLD_TYPEFACE, (int) getResources().getDimension(R.dimen.text_size_large));
            mColonPaint = createTextPaint(getColor(R.color.grey_700),
                    BOLD_TYPEFACE, (int) getResources().getDimension(R.dimen.text_size_large));
            mAMPMPaint = createTextPaint(getColor(R.color.grey_700));

            mWeatherBgPaint = new Paint();
            mWeatherBgPaint.setAntiAlias(true);
            mWeatherBgPaint.setColor(getColor(R.color.blue));
            mWeatherOuterCirclePaint = new Paint(mWeatherBgPaint);
            mWeatherOuterCirclePaint.setStyle(Paint.Style.STROKE);
            mMaxTempPaint = createTextPaint(getColor(R.color.white),
                    BOLD_TYPEFACE);
            mMinTempPaint = createTextPaint(getColor(R.color.grey),
                    NORMAL_TYPEFACE,
                    (int) getResources().getDimension(R.dimen.text_size_small));
            mDescPaint = createTextPaint(getColor(R.color.grey),
                    NORMAL_TYPEFACE,
                    (int) getResources().getDimension(R.dimen.text_size_small));

            // configure the system UI
            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle
                            .BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());


            RequestDataService.requestData(SunshineWatchFaceService.this);

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

            int scaledSize = getResources().getDimensionPixelSize(R.dimen.text_size_normal);
            paint.setTextSize(scaledSize);

            return paint;
        }

        private Paint createTextPaint(int color, Typeface typeface, int scaledSize) {
            Paint paint = createTextPaint(color, typeface);

            paint.setTextSize(scaledSize);

            return paint;
        }


        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            Utility.logD(LOG_TAG, "onPropertiesChanged");

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION,
                    false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();

            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            Utility.logD(LOG_TAG, "onAmbientModeChanged");

            //set paint colors
            //set anti alias
            //invalidate


            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;

                mClockBgPaint.setAntiAlias(antiAlias);
                mHrPaint.setAntiAlias(antiAlias);
                mColonPaint.setAntiAlias(antiAlias);
                mMinPaint.setAntiAlias(antiAlias);
                mAMPMPaint.setAntiAlias(antiAlias);
                mWeatherBgPaint.setAntiAlias(antiAlias);
                mWeatherOuterCirclePaint.setAntiAlias(antiAlias);
                mMaxTempPaint.setAntiAlias(antiAlias);
                mMinTempPaint.setAntiAlias(antiAlias);
                mDescPaint.setAntiAlias(antiAlias);
            }

            if (mBurnInProtection) {
                adjustTypefaceForBurnInProtection(mHrPaint);
                adjustTypefaceForBurnInProtection(mMinPaint);

                adjustTypefaceForBurnInProtection(mColonPaint);
                adjustTypefaceForBurnInProtection(mMaxTempPaint);
            }

            invalidate();

        }

        private void adjustTypefaceForBurnInProtection(Paint paint) {
            if (isInAmbientMode()) {
                paint.setTypeface(NORMAL_TYPEFACE);
            } else {
                paint.setTypeface(BOLD_TYPEFACE);
            }
        }


        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        private String getAmPmString(int amPm) {
            return amPm == java.util.Calendar.AM ? "AM" : "PM";
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

            canvas.drawColor(mBgColor);


            int clockXOffset = (int) getResources().getDimension(R.dimen.clock_pos_x_offset);
            int clockYOffset = (int) getResources().getDimension(R.dimen.clock_pos_y_offset);

            // allocate a Calendar to calculate local time using the UTC time and time zone
            mCalendar = Calendar.getInstance();

            float seconds = mCalendar.get(Calendar.SECOND);
            String secondsString = String.valueOf(seconds);
            float minutes = mCalendar.get(Calendar.MINUTE);
            String minuteString = formatTwoDigitNumber((int) minutes);
            float hours = mCalendar.get(Calendar.HOUR_OF_DAY);
            String hoursString = formatTwoDigitNumber((int) hours);
            String colonString = ":";
            float mColonWidth = getResources().getDimension(R.dimen.colon_width);


            float x = clockXOffset;

            canvas.drawText(hoursString, x, clockYOffset, mHrPaint);
            x += mHrPaint.measureText(hoursString);


            x += mColonWidth / 2;
            canvas.drawText(colonString, x, clockYOffset, mColonPaint);
            x += mColonWidth / 2 + mColonPaint.measureText(":");

            // Draw the minutes.
            canvas.drawText(minuteString, x, clockYOffset, mMinPaint);

            canvas.drawText(getAmPmString(
                    mCalendar.get(java.util.Calendar.AM_PM)),
                    clockXOffset,
                    clockYOffset + getResources().getDimension(R.dimen.text_size_large),
                    mAMPMPaint);

        }

        private void drawWeather(Canvas canvas, Rect bounds) {

            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches
            // with a "chin", the watch face is centered on the entire screen, not
            // just the usable portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            if (!isInAmbientMode()) {
                int xShift = (int) getResources().getDimension(R.dimen.weather_pos_x_shift);
                int yShift = (int) getResources().getDimension(R.dimen.weather_pos_y_shift);

                //Clock circle and marks
                canvas.drawCircle(width - xShift, height - yShift, width * 0.3f, mWeatherBgPaint);
                canvas.drawCircle(width - xShift, height - yShift, width * 0.4f, mWeatherOuterCirclePaint);
            }

            int x = width - (int) getResources().getDimension(R.dimen.weather_temp_x_shift);
            int y = (int) (height - getResources().getDimension(R.dimen.weather_temp_y_shift)
                    + getResources().getDimension(R.dimen.text_size_large));


            if (!Double.isNaN(mHigh)) {

                String maxTemp = Utility.formatTemperature(SunshineWatchFaceService.this,
                        mHigh);
                String minTemp = Utility.formatTemperature(SunshineWatchFaceService.this,
                        mLow);

                canvas.drawText(maxTemp, x, y, mMaxTempPaint);
                x += mMaxTempPaint.measureText(maxTemp + " ");
                canvas.drawText(minTemp,
                        x,
                        y,
                        mMinTempPaint);
            }

            int iconXShift = (int) getResources().getDimension(R.dimen.weather_icon_x_shift);
            int iconYShift = (int) getResources().getDimension(R.dimen.weather_icon_y_shift);

            if (mIconBitmap != null && !isInAmbientMode()) {
                canvas.drawBitmap(mIconBitmap, width - iconXShift, height - iconYShift, null);
            } else if (mWeatherDesc != null) {
                x = width - (int) getResources().getDimension(R.dimen.weather_temp_x_shift);
                y = (int) (height - iconYShift + getResources().getDimension(R.dimen.text_size_large));
                canvas.drawText(mWeatherDesc, x, y, mDescPaint);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                if (mGoogleApiClient != null) {
                    mGoogleApiClient.connect();
                }
            } else {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
            }
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
            Utility.logD(LOG_TAG, "onSharedPreferenceChangedListener: " + s);

            if (s.equals(PREF_DATA_CHANGED_KEY) && sharedPreferences.getBoolean(PREF_DATA_CHANGED_KEY, false)) {

                //Resetting PREF_DATA_CHANGED
                PreferenceManager
                        .getDefaultSharedPreferences(SunshineWatchFaceService.this)
                        .edit()
                        .putBoolean(SunshineWatchFaceService.PREF_DATA_CHANGED_KEY, false)
                        .apply();

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

                                                mHigh = dataMap.getDouble(HIGH_TEMP_KEY);
                                                mLow = dataMap.getDouble(LOW_TEMP_KEY);
                                                mWeatherDesc = dataMap.getString(DESC_KEY);
                                                mIconAsset = dataMap.getAsset(IMAGE_ASSET_KEY);
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        if (mIconAsset == null) {
                                                            throw new IllegalArgumentException("Asset must be non-null");
                                                        }
                                                        ConnectionResult result =
                                                                mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
                                                        if (!result.isSuccess()) {
                                                            return;
                                                        }
                                                        // convert asset into a file descriptor and block until it's ready
                                                        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                                                                mGoogleApiClient, mIconAsset).await().getInputStream();
                                                        mGoogleApiClient.disconnect();

                                                        if (assetInputStream == null) {
                                                            Log.w(LOG_TAG, "Requested an unknown Asset.");
                                                            return;
                                                        }

                                                        // decode the stream into a bitmap
                                                        Bitmap mBackgroundBitmap = BitmapFactory.decodeStream(assetInputStream);

                                                        int size = (int) getResources().getDimension(R.dimen.bitmap_height);
                                                        mIconBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                                                                size, size, true /* filter */);

                                                        invalidate();
                                                    }

                                                }).start();

                                                invalidate();
                                                Utility.logD(LOG_TAG, "Got data, desc: " + mWeatherDesc);

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
