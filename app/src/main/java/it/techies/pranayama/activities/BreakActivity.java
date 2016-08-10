package it.techies.pranayama.activities;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.timing.AasanInformation;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.infrastructure.BaseBoundActivity;
import it.techies.pranayama.modules.aasans.AasanActivity;
import it.techies.pranayama.modules.launcher.LauncherActivity;

public class BreakActivity extends BaseBoundActivity {

    private static final String TAG = "BreakActivity";

    @Bind(R.id.timer_tv)
    TextView mTimerTextView;

    // current aasan index
    private Integer mCurrentAasanIndex;

    // aasan information
    private AasanInformation mAasanInformation;

    private CountDownTimer mCountDownTimer;

    @Bind(R.id.active_pin_iv)
    ImageView mActivePinImageView;

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

        // get current aasan index
        mCurrentAasanIndex = mAasanInformation.getCurrentAasanIndex();

        // get current aasan information from aasan list
        AasanTime aasanTime = mAasanInformation.getAasanTimes().get(mCurrentAasanIndex);

        mTimerTextView.setText(aasanTime.getTimings().getBreakTimeString());

        long timer = aasanTime.getBreakTime() * 1000;

        startAnimation(timer);

        mCountDownTimer = new CountDownTimer(timer, 1000) {
            @Override
            public void onTick(long millisUntilFinished)
            {
                // millisUntilFinished = millisUntilFinished - 1000;

                long seconds = millisUntilFinished / 1000 % 60;
                long minutes = millisUntilFinished / (60 * 1000) % 60;

                mTimerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish()
            {
                Log.d(TAG, "onFinish() called with: " + "");
                mTimerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                startNextAasan();
            }
        };

        mCountDownTimer.start();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        isBreakActivity = true;
    }

    private void startAnimation(long timer)
    {
        // update timer
        ObjectAnimator animator = ObjectAnimator.ofFloat(mActivePinImageView, "rotation", 0f, 360f);
        animator.setDuration(timer);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
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

                        Intent intent = new Intent(BreakActivity.this, LauncherActivity.class);
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
            Log.d(TAG, "startNextAasan: play yoga music...");
        }
        else
        {
            Log.d(TAG, "startNextAasan: service not bound yet");
        }

        // get current aasan index
        mCurrentAasanIndex = mAasanInformation.getCurrentAasanIndex();

        final AasanTime currentAasan = mAasanInformation.getAasanTimes().get(mCurrentAasanIndex);
        final int totalSets = currentAasan.getSet();
        final boolean isLastSet = (mAasanInformation.getCurrentSetIndex() == totalSets);

        if (isLastSet)
        {
            // start the next aasan
            mAasanInformation.setCurrentAasanIndex(mCurrentAasanIndex + 1);
            // start the first set of next aasan
            mAasanInformation.setCurrentSetIndex(1);
        }
        else
        {
            // start the next aasan
            int nextSet = mAasanInformation.getCurrentSetIndex() + 1;
            if (nextSet <= totalSets)
            {
                mAasanInformation.setCurrentSetIndex(nextSet);
            }
        }

        Intent intent = new Intent(this, AasanActivity.class);
        startActivity(intent);
        finish();
    }

}
