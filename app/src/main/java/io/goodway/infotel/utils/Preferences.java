package io.goodway.infotel.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.goodway.infotel.R;

/**
 * Created by antoine on 5/14/16.
 */
public class Preferences {

    public static String getStringPreference(Context context, String key){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, null);
    }

    public static void addStringPreference(Context context, String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static void removeStringPreference(Context context, String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }
}
