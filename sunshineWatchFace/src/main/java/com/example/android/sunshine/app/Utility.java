/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

public class Utility {
    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

//
//    public static boolean isMetric(Context context) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        return prefs.getString(context.getString(R.string.pref_units_key),
//                context.getString(R.string.pref_units_metric))
//                .equals(context.getString(R.string.pref_units_metric));
//    }
//
public static String formatTemperature(Context context, double temperature) {
    // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
    // the values here.
    String suffix = "\u00B0";

//        if (!isMetric(context)) {
//            temperature = (temperature * 1.8) + 32;
//        }

    // For presentation, assume the user doesn't care about tenths of a degree.
    return String.format(context.getString(R.string.format_temperature), temperature);
}
//
//
//    /**
//     * Given a day, returns just the name to use for that day.
//     * E.g "today", "tomorrow", "wednesday".
//     *
//     * @param context      Context to use for resource localization
//     * @param dateInMillis The date in milliseconds
//     * @return
//     */
//    public static String getDayName(Context context, long dateInMillis) {
//        // If the date is today, return the localized version of "Today" instead of the actual
//        // day name.
//
//        Time t = new Time();
//        t.setToNow();
//        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
//        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
//        if (julianDay == currentJulianDay) {
//            return context.getString(R.string.today);
//        } else if (julianDay == currentJulianDay + 1) {
//            return context.getString(R.string.tomorrow);
//        } else {
//            Time time = new Time();
//            time.setToNow();
//            // Otherwise, the format is just the day of the week (e.g "Wednesday".
//            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
//            return dayFormat.format(dateInMillis);
//        }
//    }
//
//
//    /**
//     * Helper method to provide the icon resource id according to the weather condition id returned
//     * by the OpenWeatherMap call.
//     *
//     * @param weatherId from OpenWeatherMap API response
//     * @return resource id for the corresponding icon. -1 if no relation is found.
//     */
//    public static int getIconResourceForWeatherCondition(int weatherId) {

//    }
//
//
//    /**
//     * Helper method to provide the string according to the weather
//     * condition id returned by the OpenWeatherMap call.
//     *
//     * @param context   Android context
//     * @param weatherId from OpenWeatherMap API response
//     * @return string for the weather condition. null if no relation is found.
//     */
//    public static String getStringForWeatherCondition(Context context, int weatherId) {

//    }

    @SuppressLint("LogTagMismatch")
    public static void logD(String TAG, String msg){
        if(TAG.length() > 23){
            TAG = TAG.substring(0, 22);
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, msg);
        }
        else{
            //Using log.e since log.d not working
            Log.e(TAG, msg);
        }
    }
}