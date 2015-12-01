package it.techies.pranayama.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class SessionStorage
{
    private static final String EMAIL_KEY = "EMAIL_KEY";

    private static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN_KEY";

    private static final String USER_ID_KEY = "USER_ID_KEY";

    private SharedPreferences mSharedPreferences;

    public SessionStorage(Context context)
    {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean hasUserData()
    {
        String email =  mSharedPreferences.getString(EMAIL_KEY, "");
        String accessToken = mSharedPreferences.getString(ACCESS_TOKEN_KEY, "");

        return !TextUtils.isEmpty(email) && !TextUtils.isEmpty(accessToken);
    }

    public void clearUserData()
    {
        setAccessToken("");
        setEmail("");
    }

    public String getAccessToken()
    {
        return mSharedPreferences.getString(ACCESS_TOKEN_KEY, "");
    }

    /* Access token */
    public void setAccessToken(String accessToken)
    {
        mSharedPreferences.edit().putString(ACCESS_TOKEN_KEY, accessToken).apply();
    }

    public String getEmail()
    {
        return mSharedPreferences.getString(EMAIL_KEY, "");
    }

    public void setEmail(String email)
    {
        mSharedPreferences.edit().putString(EMAIL_KEY, email).apply();
    }


    public void setUserId(Integer userId)
    {
        mSharedPreferences.edit().putInt(USER_ID_KEY, userId).apply();
    }

    public Integer getUserId()
    {
        return mSharedPreferences.getInt(USER_ID_KEY, 0);
    }
}
