package it.techies.pranayama.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

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

    @Bind(R.id.toggle_btn)
    FloatingActionButton mToggleButton;

    @Bind(R.id.skip_btn)
    FloatingActionButton mSkipButton;

    @Bind(R.id.stop_btn)
    FloatingActionButton mStopButton;

    @Bind(R.id.active_pin_iv)
    ImageView mActivePinImageView;

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

    ObjectAnimator animator;

    @OnClick(R.id.stop_btn)
    public void stopButtonClick(FloatingActionButton button)
    {
        Timber.d("stop button click");

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
    public void skipButtonClick(FloatingActionButton button)
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

    @OnClick(R.id.toggle_btn)
    public void toggleButtonClick(FloatingActionButton button)
    {
        Timber.d("Toggle button is checked %s", button.getTag());

        if (button.getTag() == null)
        {
            // pause the timer
            pauseAnimation();
            pauseTimer();
            button.setTag("resume");
            button.setImageResource(R.drawable.ic_play_arrow_white_24dp);

            mSkipButton.show();
            mStopButton.show();
        }
        else
        {
            if (String.valueOf(button.getTag()).equals("resume"))
            {
                // resume the timer
                resumeAnimation();
                resumeTimer();
                button.setTag("pause");
                button.setImageResource(R.drawable.ic_pause_white_24dp);

                mSkipButton.hide();
                mStopButton.hide();
            }
            else
            {
                // pause the timer
                pauseAnimation();
                pauseTimer();
                button.setTag("resume");
                button.setImageResource(R.drawable.ic_play_arrow_white_24dp);

                mSkipButton.show();
                mStopButton.show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aasan);
        ButterKnife.bind(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSkipButton.hide();
        mStopButton.hide();

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

    private void startAnimation(long timer)
    {
        // update timer
        animator = ObjectAnimator.ofFloat(mActivePinImageView, "rotation", 0f, 360f);
        animator.setDuration(timer);
        animator.setRepeatCount(totalSets);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatMode(ObjectAnimator.INFINITE);
        animator.start();
    }

    private void pauseAnimation()
    {
        if (animator != null)
        {
            if (animator.isRunning())
            {
                animator.end();
            }
        }
    }

    private void resumeAnimation()
    {
        if (animator != null)
        {
            if (animator.isPaused())
            {
                animator.start();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        showStopAasanDialog();
    }

    /**
     * Start countdown timer.
     */
    private void startTimer(boolean addOne)
    {
        final long timer;

        if (addOne)
        {
            timer = timerSeconds + 1000;

            mTimerTextView.setText(String.format(
                    "%02d:%02d",
                    (timerSeconds / 60 * 1000) % 60,
                    (timerSeconds / (60 * 1000) % 60))
            );
        }
        else
        {
            timer = timerSeconds;
        }

        startAnimation(timer);

        mCountDownTimer = new CountDownTimer(timer, 1000) {

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
        startTimer(true);
    }

    /**
     * Resume the timer after pause.
     */
    private void resumeTimer()
    {
        startTimer(false);
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
