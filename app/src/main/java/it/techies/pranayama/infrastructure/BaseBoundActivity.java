package it.techies.pranayama.infrastructure;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import it.techies.pranayama.services.PrayanamaService;
import timber.log.Timber;

/**
 * Created by jagdeep on 15/02/16.
 */
public class BaseBoundActivity extends BaseActivity {

    protected PrayanamaService mService;
    protected boolean mBound = false;

    @Override
    protected void onStart()
    {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, PrayanamaService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        // Unbind from the service
        if (mBound)
        {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service)
        {
            Timber.d("onServiceConnected()");

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PrayanamaService.LocalBinder binder = (PrayanamaService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            Timber.d("onServiceDisconnected()");
            mBound = false;
        }
    };

}
