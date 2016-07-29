package it.techies.pranayama.activities;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

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
import it.techies.pranayama.modules.launcher.LauncherActivity;
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
     * Total number of mSets in current aasan.
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

    /**
     * Shows the stop aasan confirmation dialog.
     */
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

                        Intent intent = new Intent(AasanActivity.this, LauncherActivity.class);
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

        // get current aasan index
        mCurrentAasanIndex = mAasanInformation.getCurrentAasanIndex();

        AasanTime currentAasan = mAasanInformation.getAasanTimes().get(mCurrentAasanIndex);
        int totalSets = currentAasan.getSet();

        boolean isLastAasan = (mCurrentAasanIndex + 1 == mAasanInformation.getAasanTimes().size());
        boolean isLastSet = (mAasanInformation.getCurrentSetIndex() == totalSets);

        // check if this aasan is the last one
        if (isLastAasan)
        {
            showFinalScreen();
        }
        else
        {
            // if the current aasan set is not last, then mark it as last one, because we want to
            // skip the entire aasan if user taps on skip button.
            if (!isLastSet)
            {
                mAasanInformation.setCurrentSetIndex(totalSets);
            }

            startNextAasan();
        }
    }

    private void startNextAasan()
    {
        // get current aasan index
        int currentAasanIndex = mAasanInformation.getCurrentAasanIndex();

        final AasanTime currentAasan = mAasanInformation.getAasanTimes().get(currentAasanIndex);
        final int totalSets = currentAasan.getSet();
        final boolean isLastSet = (mAasanInformation.getCurrentSetIndex() == totalSets);

        if (isLastSet)
        {
            // start the next aasan
            mAasanInformation.setCurrentAasanIndex(currentAasanIndex + 1);
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

    protected void playBellMusic()
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

        mSkipButton.hide();
        mStopButton.hide();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        Timber.tag(AasanActivity.class.getSimpleName());

        // get aasan's information
        // mAasanInformation = getIntent().getParcelableExtra(LauncherActivity.AASAN_LIST_KEY);

        // get daily routine information
        // mDailyRoutine = getIntent().getParcelableExtra(LauncherActivity.DAILY_ROUTINE_KEY);

        // get current aasan index
        mCurrentAasanIndex = mAasanInformation.getCurrentAasanIndex();

        // read number of mSets in pranayama
        currentSet = mAasanInformation.getCurrentSetIndex();

        // get current aasan information from aasan list
        final AasanTime aasanTime = mAasanInformation.getAasanTimes().get(mCurrentAasanIndex);


        // update the title bar with the name of current aasan
        ab.setTitle(aasanTime.getName());

        // total mSets in current aasan
        totalSets = aasanTime.getSet();

        // update the current aasan and total aasan on screen
        mSetTextView.setText(String.format("%d of %d", currentSet, totalSets));

        // show break time on view
        Timings breakTimings = new Timings("00:00:00");
        breakTimings.addSeconds(aasanTime.getBreakTime());
        mBreakTimeTextView.setText(breakTimings.getBreakTimeString());

        Timings timings = aasanTime.getTimings();
        mSingleSetDuration = timings.getSingleSetDuration();
        createTimer(mSingleSetDuration);

        setupBenefitTextView(aasanTime);
    }

    private void setupBenefitTextView(AasanTime aasanTime)
    {
        String benefits = "N/A";

        switch (aasanTime.getName())
        {
            case AasanNames.Bhastrika:
                benefits = getString(R.string.benefit_bhastrika);
                break;

            case AasanNames.Kapalbhati:
                benefits = getString(R.string.benefit_kapalbhati);
                break;

            case AasanNames.Bahaya:
                benefits = getString(R.string.benefit_bahaya);
                break;

            case AasanNames.Agnisar_Kriya:
                benefits = getString(R.string.benefit_agnisar_kriya);
                break;

            case AasanNames.Anulom_Vilom:
                benefits = getString(R.string.benefit_anulom_vilom);
                break;

            case AasanNames.Bharmari:
                benefits = getString(R.string.benefit_bharamri);
                break;

            case AasanNames.Udgeeth:
                benefits = getString(R.string.benefit_udgeeth);
                break;
        }

        mBenefitsTextView.setText(benefits);
    }

    private void startAnimation(long timer)
    {
        // update timer
        animator = ObjectAnimator.ofFloat(mActivePinImageView, "rotation", 0f, 360f);
        animator.setDuration(timer);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    /**
     * Pause clock animation.
     */
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

    /**
     * Resume clock animation.
     */
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

                startBreak();
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
        playBellMusic();

        // get current aasan index
        mCurrentAasanIndex = mAasanInformation.getCurrentAasanIndex();

        AasanTime currentAasan = mAasanInformation.getAasanTimes().get(mCurrentAasanIndex);
        int totalSets = currentAasan.getSet();

        boolean isLastAasan = (mCurrentAasanIndex + 1 == mAasanInformation.getAasanTimes().size());
        boolean isLastSet = (mAasanInformation.getCurrentSetIndex() == totalSets);

        // check if this aasan is the last one
        if (isLastAasan && isLastSet)
        {
            showFinalScreen();
        }
        else
        {
            Intent intent = new Intent(this, BreakActivity.class);
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
            case AasanNames.Bahaya:
                mDailyRoutine.setBahaya("1");
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
        startActivity(intent);
        finish();
    }

}
