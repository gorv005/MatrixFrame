package com.app.matrixframe.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by gaurav.garg on 08-09-2016.
 */
public class SharedPreference {

    private Context context;
    private static SharedPreference savePreferenceAndData;

    public static SharedPreference getInstance(Context context)
    {
        if(savePreferenceAndData==null)
        {
            savePreferenceAndData = new SharedPreference(context);
        }
        return savePreferenceAndData;
    }


    public SharedPreference(Context context)
    {
        this.context = context;

    }
    public void setString(String key, String data)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(key, data).apply();
    }

    public void setBoolean(String key, boolean status)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(key, status).apply();
    }
    public boolean getBoolean(String key)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return  prefs.getBoolean(key, false);
    }
    public String getString(String key)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return  prefs.getString(key, null);
    }

    public void clearData()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().clear().commit();
    }
}
