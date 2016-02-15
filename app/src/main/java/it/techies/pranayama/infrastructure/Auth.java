package it.techies.pranayama.infrastructure;

import android.content.Context;
import android.support.annotation.Nullable;

import it.techies.pranayama.utils.ApplicationSettings;
import timber.log.Timber;

/**
 * Created by jagdeep on 28/01/16.
 */
public class Auth {
    private String token;

    private User user;

    private TokenChangeListener mTokenChangeListener;

    public Auth(String token, User user)
    {
        this.token = token;
        this.user = user;
    }

    public void setTokenChangeListener(TokenChangeListener tokenChangeListener)
    {
        mTokenChangeListener = tokenChangeListener;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(Context context, String token)
    {
        this.token = token;

        // update token in prefs
        ApplicationSettings settings = new ApplicationSettings(context);
        if (token == null)
        {
            settings.setTokenPreference("");
        }
        else
        {
            settings.setTokenPreference(token);
        }

        // notify the listeners
        if (mTokenChangeListener != null)
        {
            mTokenChangeListener.onTokenChanged(token);
        }
        else
        {
            Timber.d("mTokenChangeListener is null");
        }
    }

    public boolean isLoggedIn()
    {
        if (this.getToken() != null && !this.getToken().isEmpty())
        {
            if (this.getUser() != null)
            {
                if (this.getUser().getEmail() != null && !this.getUser().getEmail().isEmpty())
                {
                    if (this.getUser().getUserId() != null && this.getUser().getUserId() != 0)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void logout(Context context)
    {
        ApplicationSettings settings = new ApplicationSettings(context);
        settings.clearUserData();
        this.setToken(context, null);
        this.setUser(new User());
    }

    public static Auth getAuth(Context context)
    {
        ApplicationSettings settings = new ApplicationSettings(context);
        return new Auth(settings.getTokenPreference(), User.getUser(settings));
    }

    public void update(Context context)
    {
        ApplicationSettings settings = new ApplicationSettings(context);
        settings.setTokenPreference(this.token);
        this.user.update(settings);
    }

    public interface TokenChangeListener {
        void onTokenChanged(@Nullable String token);
    }
}
