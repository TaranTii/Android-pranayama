package it.techies.pranayama.infrastructure;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import it.techies.pranayama.R;
import it.techies.pranayama.services.PrayanamaService;
import me.alexrs.prefs.lib.Prefs;

/**
 * Created by jagdeep on 15/02/16.
 */
public class BaseBoundActivity extends BaseActivity {

    private static final String TAG = "BaseBoundActivity";

    protected PrayanamaService mService;
    protected boolean mBound = false;
    protected Menu mMenu;
    protected boolean isBreakActivity = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (Prefs.with(this).getBoolean(getString(R.string.pref_key_music), true) && !isBreakActivity)
        {
            // Bind to LocalService
            Intent intent = new Intent(this, PrayanamaService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
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

    @Override
    protected void onResume()
    {
        super.onResume();
        checkMuteMenuButton();
    }

    private void checkMuteMenuButton()
    {
        if (mMenu != null)
        {
            MenuItem menuItem = mMenu.findItem(R.id.action_mute);

            if (Prefs.with(this).getBoolean(getString(R.string.pref_key_music), true))
            {
                menuItem.setVisible(true);
            }
            else
            {
                menuItem.setVisible(false);
            }
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
            Log.d(TAG, "onServiceConnected() called with: " + "className = [" + className + "], service = [" + service + "]");

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PrayanamaService.LocalBinder binder = (PrayanamaService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            Log.d(TAG, "onServiceDisconnected() called with: " + "arg0 = [" + arg0 + "]");
            mBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_mute, menu);
        mMenu = menu;
        checkMuteMenuButton();

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int v = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (v > 0)
        {
            // un mute
            if (mMenu.size() > 0)
            {
                MenuItem menuItem = mMenu.findItem(R.id.action_mute);
                menuItem.setTitle("Mute");
            }
        }
        else
        {
            // mute
            if (mMenu.size() > 0)
            {
                MenuItem menuItem = mMenu.findItem(R.id.action_mute);
                menuItem.setTitle("Unmute");
            }
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_mute)
        {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            int v = am.getStreamVolume(AudioManager.STREAM_MUSIC);

            if (v > 0)
            {
                // un mute
                if (mMenu.size() > 0)
                {
                    MenuItem menuItem = mMenu.findItem(R.id.action_mute);
                    menuItem.setTitle("Unmute");
                }
                // un mute
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
            }
            else
            {
                int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                // mute
                am.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);

                // mute
                if (mMenu.size() > 0)
                {
                    MenuItem menuItem = mMenu.findItem(R.id.action_mute);
                    menuItem.setTitle("Mute");
                }
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
