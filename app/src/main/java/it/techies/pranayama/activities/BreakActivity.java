package it.techies.pranayama.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.timing.AasanInformation;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.infrastructure.BaseBoundActivity;
import timber.log.Timber;

public class BreakActivity extends BaseBoundActivity {

    @Bind(R.id.timer_tv)
    TextView mTimerTextView;

    // current aasan index
    private Integer currentAasanIndex;

    // aasan information
    private AasanInformation aasanInformation;

    private CountDownTimer mCountDownTimer;

    @OnClick(R.id.skip_btn)
    public void skipButtonClick(View v)
    {
        if (mCountDownTimer != null)
        {
            mCountDownTimer.cancel();
        }

        startNextAasan();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        aasanInformation = getIntent().getParcelableExtra(MainActivity.AASAN_LIST_KEY);

        // get current aasan index
        currentAasanIndex = aasanInformation.getCurrentAasanIndex();

        // get current aasan information from aasan list
        AasanTime aasanTime = aasanInformation.getAasanTimes().get(currentAasanIndex);

        mTimerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", 10, 0));
        mCountDownTimer = new CountDownTimer(10 * 1000 + 2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished)
            {
                millisUntilFinished = millisUntilFinished - 1000;

                long seconds = millisUntilFinished / 1000 % 60;
                long minutes = millisUntilFinished / (60 * 1000) % 60;

                mTimerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish()
            {
                Timber.d("break timer finished...");
                mTimerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                startNextAasan();
            }
        };

        mCountDownTimer.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mCountDownTimer.cancel();
    }

    @Override
    public void onBackPressed()
    {
        showStopAasanDialog();
    }

    private void showStopAasanDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle("Stop the Prayanama?")
                .setPositiveButton("Stop", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (mCountDownTimer != null)
                        {
                            mCountDownTimer.cancel();
                        }

                        Intent intent = new Intent(BreakActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void startNextAasan()
    {
        if (mBound)
        {
            mService.playYogaMusic();
            Timber.d("play yoga music...");
        }
        else
        {
            Timber.d("service not bound yet");
        }

        // get current aasan index
        currentAasanIndex = aasanInformation.getCurrentAasanIndex();

        // update current aasan index
        aasanInformation.setCurrentAasanIndex(currentAasanIndex + 1);

        Intent intent = new Intent(this, AasanActivity.class);
        intent.putExtra(MainActivity.AASAN_LIST_KEY, aasanInformation);
        intent.putExtra(MainActivity.DAILY_ROUTINE_KEY, getIntent().getParcelableExtra(MainActivity.DAILY_ROUTINE_KEY));
        startActivity(intent);
        finish();
    }

}
