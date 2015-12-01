package it.techies.pranayama;

import android.app.Application;

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
