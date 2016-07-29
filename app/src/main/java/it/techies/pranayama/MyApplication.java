package it.techies.pranayama;

import android.app.Application;

import com.squareup.otto.Bus;

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
        // MultiDex.install(this);
        super.onCreate();

        initializeTimber();

        Timber.tag("MyApplication");
        Timber.d("onCreate()");
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
}
