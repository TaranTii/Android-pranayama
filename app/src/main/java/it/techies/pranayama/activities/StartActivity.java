package it.techies.pranayama.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import it.techies.pranayama.utils.SessionStorage;

public class StartActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SessionStorage sessionStorage = new SessionStorage(this);

        if (sessionStorage.hasUserData())
        {
            // open main page
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            // goto login page
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
