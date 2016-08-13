package it.techies.pranayama.modules.aasans.base;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.modules.aasanBreak.BreakActivity;
import it.techies.pranayama.activities.EndActivity;
import it.techies.pranayama.infrastructure.BaseBoundActivity;
import it.techies.pranayama.models.FirebaseHistory;
import it.techies.pranayama.models.FirebaseSchedule;
import it.techies.pranayama.modules.aasans.Udgeeth;
import it.techies.pranayama.modules.aasans.model.CurrentAasan;
import it.techies.pranayama.modules.launcher.LauncherActivity;
import it.techies.pranayama.utils.FireRef;
import it.techies.pranayama.utils.Utils;

/**
 * Base activity for all aasans.
 * <p/>
 * Created by jagdeep on 10/08/16.
 */
public abstract class BaseAasanActivity extends BaseBoundActivity {

    private static final String TAG = "BaseAasanActivity";

    public static final String KEY_CURRENT_AASAN = "key_current_aasan";

    @Bind(R.id.timer_tv)
    TextView mTimerTextView;

    @Bind(R.id.set_tv)
    TextView mSetTextView;

    @Bind(R.id.break_time_tv)
    TextView mBreakTimeTextView;

    @Bind(R.id.benefits_tv)
    TextView mBenefitsTextView;

    @Bind(R.id.skip_btn)
    FloatingActionButton mSkipButton;

    @Bind(R.id.stop_btn)
    FloatingActionButton mStopButton;

    @Bind(R.id.active_pin_iv)
    ImageView mActivePinImageView;

    /**
     * CountDownTimer
     */
    protected CountDownTimer mCountDownTimer;

    /**
     * Count down timer seconds
     */
    protected long mTimerSeconds;

    /**
     * Current aasan.
     */
    protected CurrentAasan mCurrentAasan;

    /**
     * Details of current aasan schedule.
     */
    protected FirebaseSchedule mSchedule;

    /**
     * ObjectAnimator for timer animation.
     */
    protected ObjectAnimator mObjectAnimator;

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
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setTitle(getActionBarTitle());
        }

        // read current aasan set number
        if (getIntent().hasExtra(KEY_CURRENT_AASAN))
        {
            mCurrentAasan = getIntent().getParcelableExtra(KEY_CURRENT_AASAN);

            // set appropriate next aasan and current aasan class
            if (mCurrentAasan.getCurrentSet() == 1)
            {
                mCurrentAasan = getUpdatedCurrentAasan();
            }
        }
        else
        {
            throw new IllegalArgumentException("Needs CurrentAasan object");
        }

        setupBenefitTextView();

        setupBreakTimeText();

        String aasanName = getAasanName();

        DatabaseReference mRef = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_USER_PREFS)
                .child(getUid())
                .child(aasanName);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mSchedule = dataSnapshot.getValue(FirebaseSchedule.class);

                mCurrentAasan.setNumberOfSets(mSchedule.numberOfSets);

                String setsText = String.format(
                        Locale.getDefault(),
                        "%d of %d",
                        mCurrentAasan.getCurrentSet(),
                        mSchedule.numberOfSets);

                // update the current aasan and total aasan on screen
                mSetTextView.setText(setsText);

                createTimer(mSchedule.duration);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                showToast(databaseError.getMessage());
            }
        });

    }

    public CurrentAasan getUpdatedCurrentAasan()
    {
        return new CurrentAasan(
                getAasanName(),
                getClass(),
                getNextAasanClass()
        );
    }

    public void startNextAasan()
    {
        Intent intent = new Intent(this, getNextAasanClass());
        CurrentAasan currentAasan = getUpdatedCurrentAasan();
        intent.putExtra(BaseAasanActivity.KEY_CURRENT_AASAN, currentAasan);
        startActivity(intent);
        finish();
    }

    public abstract String getActionBarTitle();

    public abstract String getAasanName();

    public abstract String getAasanBenefits();

    public abstract Class<?> getNextAasanClass();

    protected boolean isLastAasan()
    {
        return false;
    }

    /**
     * Reads the break time from firebase and set text in BreakTime textView.
     */
    private void setupBreakTimeText()
    {
        DatabaseReference mRef = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_USER_PREFS)
                .child(getUid())
                .child(FireRef.REF_AASAN_BREAK);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                FirebaseSchedule schedule = dataSnapshot.getValue(FirebaseSchedule.class);
                mBreakTimeTextView.setText(schedule.getTimeString() + " min");
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                showToast(databaseError.getMessage());
            }
        });
    }

    private void setupBenefitTextView()
    {
        mBenefitsTextView.setText(getAasanBenefits());
    }

    /**
     * Start clock animation.
     *
     * @param timer Animation duration
     */
    private void startAnimation(long timer)
    {
        // update timer
        mObjectAnimator = ObjectAnimator.ofFloat(mActivePinImageView, "rotation", 0f, 360f);
        mObjectAnimator.setDuration(timer);
        mObjectAnimator.setInterpolator(new LinearInterpolator());
        mObjectAnimator.start();
    }

    /**
     * Pause clock animation.
     */
    private void pauseAnimation()
    {
        if (mObjectAnimator != null)
        {
            if (mObjectAnimator.isRunning())
            {
                mObjectAnimator.end();
            }
        }
    }

    /**
     * Resume clock animation.
     */
    private void resumeAnimation()
    {
        if (mObjectAnimator != null)
        {
            if (mObjectAnimator.isPaused())
            {
                mObjectAnimator.start();
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
     *
     * @param addOne If we need to add one second in the timer.
     */
    private void startTimer(boolean addOne)
    {
        final long timer;

        if (addOne)
        {
            timer = mTimerSeconds + 1000;

            mTimerTextView.setText(String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    (mTimerSeconds / 60 * 1000) % 60,
                    (mTimerSeconds / (60 * 1000) % 60)));
        }
        else
        {
            timer = mTimerSeconds;
        }

        startAnimation(timer);

        mCountDownTimer = new CountDownTimer(timer, 1000) {

            @Override
            public void onTick(long millisUntilFinished)
            {
                mTimerSeconds = millisUntilFinished;

                long seconds = millisUntilFinished / 1000 % 60;
                long minutes = millisUntilFinished / (60 * 1000) % 60;

                mTimerTextView.setText(String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        minutes,
                        seconds));
            }

            @Override
            public void onFinish()
            {
                Log.d(TAG, "onFinish() called");

                // update the timer text
                mTimerTextView.setText(String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        0,
                        0));

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
     * @param seconds Single set duration
     */
    private void createTimer(long seconds)
    {
        mTimerSeconds = seconds * 1000L;
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
        if (isLastAasan() && mCurrentAasan.isLastSet())
        {
            showFinalScreen();
        }
        else
        {
            playBellMusic();
            BreakActivity.startActivity(this, mCurrentAasan);
        }
    }

    /**
     * Mark the aasan completed in daily routine report.
     */
    private void setIsCompleted()
    {
        // add completed set in user history
        FirebaseHistory history = new FirebaseHistory(getAasanName(), mSchedule.duration);
        history.save(getUid(), Utils.getCurrentDate());
    }

    /**
     * Show the final screen after completing the Prayanama.
     */
    protected void showFinalScreen()
    {
        Intent intent = new Intent(this, EndActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.stop_btn)
    public void stopButtonClick(FloatingActionButton b)
    {
        showStopAasanDialog();
    }

    /**
     * Shows the stop aasan confirmation dialog.
     */
    private void showStopAasanDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_stop_pranayama)
                .setPositiveButton(R.string.dialog_action_stop, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (mCountDownTimer != null)
                        {
                            mCountDownTimer.cancel();
                        }

                        Intent intent = new Intent(BaseAasanActivity.this, LauncherActivity.class);
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
        if (mCountDownTimer != null)
        {
            mCountDownTimer.cancel();
        }

        startNextAasan();
    }

    protected void playBellMusic()
    {
        if (mBound)
        {
            mService.playMeditationBellMusic();
            Log.d(TAG, "playBellMusic: play bell music...");
        }
        else
        {
            Log.d(TAG, "playBellMusic: service not bound yet");
        }
    }

    @OnClick(R.id.toggle_btn)
    public void toggleButtonClick(FloatingActionButton button)
    {
        Log.d(TAG, "toggleButtonClick: Toggle button is checked :" + button.getTag());

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

}
