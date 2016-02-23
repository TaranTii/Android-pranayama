package it.techies.pranayama.activities;

import android.content.Intent;
import android.os.Bundle;

import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseActivity;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (mAuth.isLoggedIn())
        {
            // open main page
            Intent intent = new Intent(this, LauncherActivity.class);
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
