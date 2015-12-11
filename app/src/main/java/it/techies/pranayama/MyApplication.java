package it.techies.pranayama;

import android.app.Application;

import com.facebook.FacebookSdk;

import timber.log.Timber;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class MyApplication extends Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.

        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        }
        else
        {
            // Timber.plant(new CrashReportingTree());
        }

    }
}
