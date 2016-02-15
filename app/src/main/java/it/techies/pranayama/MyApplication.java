package it.techies.pranayama;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.squareup.otto.Bus;

import io.fabric.sdk.android.Fabric;
import it.techies.pranayama.infrastructure.AndroidBus;
import it.techies.pranayama.infrastructure.Auth;
import it.techies.pranayama.infrastructure.ReleaseTree;
import timber.log.Timber;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class MyApplication extends Application {

    private Bus mBus;
    private Auth mAuth;

    @Override
    public void onCreate()
    {
        FacebookSdk.sdkInitialize(getApplicationContext());
        // MultiDex.install(this);
        super.onCreate();

        initializeFabric();

        initializeTimber();

        Timber.tag("MyApplication");
        Timber.d("onCreate()");
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        Timber.d("onTerminate()");
    }

    @Override
    public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        Timber.d("onTrimMemory()");
    }

    synchronized public Bus getBus()
    {
        if (mBus == null)
        {
            mBus = new AndroidBus();
        }
        return mBus;
    }

    synchronized public Auth getAuth()
    {
        if (mAuth == null)
        {
            mAuth = Auth.getAuth(this);
        }
        return mAuth;
    }

    synchronized public void setAuth(Auth auth)
    {
        this.mAuth = auth;
    }

    private void initializeTimber()
    {
        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree() {
                // Add the line number to the tag
                @Override
                protected String createStackElementTag(StackTraceElement element)
                {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        }
        else
        {
            Timber.plant(new ReleaseTree());
        }
    }

    private void initializeFabric()
    {
        if (!Fabric.isInitialized())
        {
            new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .debuggable(true)
                    .build();

            Fabric.with(this, new Crashlytics());
        }
    }
}
