package com.example.sunshinewatchface;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

public class SunshineWatchFaceService extends CanvasWatchFaceService {


    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final String TAG = SunshineWatchFaceService.class.getSimpleName();


    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    public class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


        int mBgColor;

        Paint mClockBgPaint;
        Paint mHrPaint;
        Paint mColonPaint;
        Paint mMinPaint;
        Paint mAMPMPaint;

        Paint mWeatherBgPaint;
        Paint mMaxTempPaint;
        Paint mMinTempPaint;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

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
        public void onConnected(@Nullable Bundle bundle) {

        }

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            for (DataEvent dataEvent : dataEventBuffer) {
                if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                    DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                    String path = dataEvent.getDataItem().getUri().getPath();

                    if (path.equals("/testData")) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "Test DataItem updated:" + dataMap.getString("test-string"));
                        }
                    }
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }
    }

}
