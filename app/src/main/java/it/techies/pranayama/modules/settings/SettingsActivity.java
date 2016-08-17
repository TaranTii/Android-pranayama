package it.techies.pranayama.modules.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import it.techies.pranayama.R;
import it.techies.pranayama.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(getString(R.string.pref_key_music)))
        {
            // stop the music
            Log.d(TAG, "onSharedPreferenceChanged: Music pref changed");

            if (sharedPreferences.getBoolean(getString(R.string.pref_key_music), false))
            {
                Log.d(TAG, "onSharedPreferenceChanged: music enabled");
            }
            else
            {
                Log.d(TAG, "onSharedPreferenceChanged: music disabled");
            }
        }
        else if (key.equals(getString(R.string.pref_key_push_notification)))
        {
            // stop the push notification
            Log.d(TAG, "onSharedPreferenceChanged: Push notifications change");
        }
    }
}
