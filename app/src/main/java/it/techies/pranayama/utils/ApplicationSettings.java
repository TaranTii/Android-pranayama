package it.techies.pranayama.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class ApplicationSettings {

    // push notification registration flags
    public static final String SENT_TOKEN_TO_SERVER = "sent_token_to_server";

    public static final String REGISTRATION_COMPLETE = "registration_complete";

    // prefs key
    public static final String EMAIL_KEY = "email_key";

    public static final String RESOURCE_ID_KEY = "resource_id_key";

    public static final String TOKEN_KEY = "token_key";

    public static final String PROFILE_PHOTO_URL_KEY = "profile_photo_url_key";

    public static final String Full_NAME_KEY = "full_name_key";

    public static final String PHONE_NUMBER_KEY = "phone_number_key";


    private SharedPreferences mSharedPreferences;

    public ApplicationSettings(Context mContext)
    {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void setOnChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
    {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unRegisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
    {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Clears all the user data in shared preferences.
     */
    public void clearUserData()
    {
        mSharedPreferences.edit().clear().apply();
    }

    /**
     * Get any string preference.
     *
     * @param key
     * @param mDefault
     * @return
     */
    public String getStringPreference(String key, String mDefault)
    {
        return mSharedPreferences.getString(key, mDefault);
    }

    /**
     * Get logged in user's email address.
     *
     * @return
     */
    public String getEmailPreference()
    {
        return mSharedPreferences.getString(EMAIL_KEY, "");
    }

    /**
     * Save user email in shared preferences.
     *
     * @param email
     */
    public void setEmailPreference(String email)
    {
        mSharedPreferences.edit().putString(EMAIL_KEY, email).apply();
    }

    /**
     * Get resource_id from shared preferences.
     *
     * @return
     */
    public int getResourceIdPreference()
    {
        return mSharedPreferences.getInt(RESOURCE_ID_KEY, 0);
    }

    /**
     * Save resource_id in shared preferences.
     *
     * @param value
     */
    public void setResourceIdPreference(int value)
    {
        mSharedPreferences.edit().putInt(RESOURCE_ID_KEY, value).apply();
    }

    /**
     * Get user token for authentication from shared preferences.
     *
     * @return
     */
    public String getTokenPreference()
    {
        return mSharedPreferences.getString(TOKEN_KEY, "");
    }

    /**
     * Save user token for authentication in shared preferences.
     *
     * @param value
     */
    public void setTokenPreference(String value)
    {
        mSharedPreferences.edit().putString(TOKEN_KEY, value).apply();
    }

    public String getFullName()
    {
        return mSharedPreferences.getString(Full_NAME_KEY, "");
    }

    // full name
    public void setFullName(String fullName)
    {
        mSharedPreferences.edit().putString(Full_NAME_KEY, fullName).apply();
    }

    public String getProfilePhotoUrl()
    {
        return mSharedPreferences.getString(PROFILE_PHOTO_URL_KEY, "");
    }

    // profile photo
    public void setProfilePhotoUrl(String url)
    {
        mSharedPreferences.edit().putString(PROFILE_PHOTO_URL_KEY, url).apply();
    }

    public String getPhoneNumber()
    {
        return mSharedPreferences.getString(PHONE_NUMBER_KEY, "");
    }

    // phone number
    public void setPhoneNumber(String phoneNumber)
    {
        mSharedPreferences.edit().putString(PHONE_NUMBER_KEY, phoneNumber).apply();
    }

}