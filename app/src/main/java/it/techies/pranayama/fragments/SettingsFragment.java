package it.techies.pranayama.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import it.techies.pranayama.R;

/**
 * Created by jagdeep on 08/03/16.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }


}
