package it.techies.pranayama.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import it.techies.pranayama.R;

/**
 * Created by jdtechies on 04/12/2015.
 */
public class BaseActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // opening transition animations
        // overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_translate);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // closing transition animations
        // overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }


    @Override
    public void startActivity(Intent intent)
    {
        super.startActivity(intent);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }
}
