package it.techies.pranayama.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.AasanNames;
import it.techies.pranayama.api.DailyRoutine;
import it.techies.pranayama.api.timing.AasanInformation;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.api.timing.Timings;
import it.techies.pranayama.infrastructure.BaseBoundActivity;
import timber.log.Timber;

public class AasanActivity extends BaseBoundActivity {

    @Bind(R.id.timer_tv)
    TextView mTimerTextView;

    @Bind(R.id.set_tv)
    TextView mSetTextView;

    @Bind(R.id.break_time_tv)
    TextView mBreakTimeTextView;

    @Bind(R.id.benefits_tv)
    TextView mBenefitsTextView;

    /**
     * Index of the current Aasan in Aasan's list.
     */
    private Integer mCurrentAasanIndex;

    /**
     * Prayanama information.
     */
    private AasanInformation mAasanInformation;

    /**
     * Daily routine information to send at the end of pranayama.
     */
    private DailyRoutine mDailyRoutine;

    /**
     * CountDownTimer
     */
    private CountDownTimer mCountDownTimer;

    /**
     * Current set counter.
     */
    private int currentSet = 1;

    /**
     * Total number of sets in current aasan.
     */
    private int totalSets;

    /**
     * Count down timer seconds
     */
    private long timerSeconds;

    /**
     * Duration of a single set.
     */
    private long mSingleSetDuration;

    @OnClick(R.id.stop_btn)
    public void stopButtonClick(View view)
    {
        Timber.d("stop button click");

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

                        Intent intent = new Intent(AasanActivity.this, MainActivity.class);
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

    @OnClick(R.id.skip_btn)
    public void skipButtonClick(View view)
    {
        Timber.d("skip button click");

        if (mCountDownTimer != null)
        {
            mCountDownTimer.cancel();
        }

        // check if this aasan is the last one
        if (mCurrentAasanIndex == mAasanInformation.getAasanTimes().size() - 1)
        {
            showFinalScreen();
        }
        else
        {
            startBreak();
        }
    }

    @OnClick(R.id.toggleButton)
    public void toggleButtonClick(ToggleButton button)
    {
        Timber.d("Toggle button is checked %b", button.isChecked());

        if (button.isChecked())
        {
            // resume the timer
            resumeTimer();
        }
        else
        {
            // pause the timer
            pauseTimer();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aasan);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        Timber.tag(AasanActivity.class.getSimpleName());

        // get aasan's information
        mAasanInformation = getIntent().getParcelableExtra(MainActivity.AASAN_LIST_KEY);

        // get daily routine information
        mDailyRoutine = getIntent().getParcelableExtra(MainActivity.DAILY_ROUTINE_KEY);

        // get current aasan index
        mCurrentAasanIndex = mAasanInformation.getCurrentAasanIndex();

        // get current aasan information from aasan list
        final AasanTime aasanTime = mAasanInformation.getAasanTimes().get(mCurrentAasanIndex);

        // update the title bar with the name of current aasan
        ab.setTitle(aasanTime.getName());

        // read number of sets in pranayama
        currentSet = 1;

        // total sets in current aasan
        totalSets = aasanTime.getSet();

        // update the current aasan and total aasan on screen
        mSetTextView.setText(String.format("%d of %d", currentSet, totalSets));

        Timings timings = aasanTime.getTimings();
        mSingleSetDuration = timings.getSingleSetDuration();
        createTimer(timings.getSingleSetDuration());
    }

    /**
     * Start countdown timer.
     */
    private void startTimer()
    {
        mTimerTextView.setText(String.format(
                        "%02d:%02d",
                        (timerSeconds / 60 * 1000) % 60,
                        (timerSeconds / (60 * 1000) % 60))
        );

        mCountDownTimer = new CountDownTimer(timerSeconds + 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished)
            {
                timerSeconds = millisUntilFinished;

                long seconds = millisUntilFinished / 1000 % 60;
                long minutes = millisUntilFinished / (60 * 1000) % 60;

                mTimerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish()
            {
                Timber.d("aasan timer finished...");

                // update the timer text
                mTimerTextView.setText(String.format("%02d:%02d", 0, 0));

                // mark the aasan completed even if one set is done
                setIsCompleted();

                if (currentSet < totalSets)
                {
                    startNextSet();
                }
                else
                {
                    startBreak();
                }
            }

        };

        // start the timer
        mCountDownTimer.start();
    }

    /**
     * Start new timer with given seconds.
     *
     * @param seconds Timer seconds8
     */
    private void createTimer(long seconds)
    {
        timerSeconds = seconds;
        startTimer();
    }

    /**
     * Resume the timer after pause.
     */
    private void resumeTimer()
    {
        startTimer();
    }

    /**
     * Pause timer.
     */
    private void pauseTimer()
    {
        mCountDownTimer.cancel();
    }

    /**
     * Start the next set of aasan.
     */
    private void startNextSet()
    {
        // update current set
        currentSet++;

        // update the current set counter on screen
        mSetTextView.setText(String.format("%d of %d", currentSet, totalSets));

        // restart the counter
        timerSeconds = mSingleSetDuration;
        mCountDownTimer.cancel();
        mCountDownTimer.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mCountDownTimer.cancel();
    }

    /**
     * Start the break.
     */
    private void startBreak()
    {
        if (mBound)
        {
            mService.playMeditationBellMusic();
            Timber.d("play bell music...");
        }
        else
        {
            Timber.d("service not bound yet");
        }

        // get current aasan index
        mCurrentAasanIndex = mAasanInformation.getCurrentAasanIndex();

        // check if this aasan is the last one
        if (mCurrentAasanIndex + 1 == mAasanInformation.getAasanTimes().size())
        {
            showFinalScreen();
        }
        else
        {
            Intent intent = new Intent(this, BreakActivity.class);
            intent.putExtra(MainActivity.DAILY_ROUTINE_KEY, mDailyRoutine);
            intent.putExtra(MainActivity.AASAN_LIST_KEY, mAasanInformation);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Mark the aasan completed in daily routine report.
     */
    private void setIsCompleted()
    {
        AasanTime aasanTime = mAasanInformation.getAasanTimes().get(mCurrentAasanIndex);
        String name = aasanTime.getName();

        // add the current aasan time in total pranayama time for daily routine
        mDailyRoutine.addTime(aasanTime.getTimings());

        switch (name)
        {
            case AasanNames.Bhastrika:
                mDailyRoutine.setBhastrika("1");
                break;
            case AasanNames.Kapalbhati:
                mDailyRoutine.setKapalbhati("1");
                break;
            case AasanNames.Bahi:
                mDailyRoutine.setBahi("1");
                break;
            case AasanNames.Agnisar_Kriya:
                mDailyRoutine.setAgnisarKriya("1");
                break;
            case AasanNames.Anulom_Vilom:
                mDailyRoutine.setAnulomVilom("1");
                break;
            case AasanNames.Bharmari:
                mDailyRoutine.setBharmari("1");
                break;
            case AasanNames.Udgeeth:
                mDailyRoutine.setUdgeeth("1");
                break;
        }
    }

    /**
     * Show the final screen after completing the Prayanama.
     */
    private void showFinalScreen()
    {
        // open the final screen
        Intent intent = new Intent(this, EndActivity.class);
        intent.putExtra(MainActivity.DAILY_ROUTINE_KEY, mDailyRoutine);
        startActivity(intent);
        finish();
    }

}
