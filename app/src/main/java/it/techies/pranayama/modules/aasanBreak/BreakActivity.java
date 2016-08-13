package it.techies.pranayama.modules.aasanBreak;

import android.animation.ObjectAnimator;
import android.content.Context;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseBoundActivity;
import it.techies.pranayama.models.FirebaseSchedule;
import it.techies.pranayama.modules.aasans.base.BaseAasanActivity;
import it.techies.pranayama.modules.aasans.model.CurrentAasan;
import it.techies.pranayama.modules.launcher.LauncherActivity;
import it.techies.pranayama.utils.FireRef;

public class BreakActivity extends BaseBoundActivity {

    private static final String TAG = "BreakActivity";

    @Bind(R.id.timer_tv)
    TextView mTimerTextView;

    @Bind(R.id.active_pin_iv)
    ImageView mActivePinImageView;

    /**
     * CountDown timer.
     */
    private CountDownTimer mCountDownTimer;

    /**
     * Information about current and next aasans.
     */
    private CurrentAasan mCurrentAasan;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent().hasExtra(BaseAasanActivity.KEY_CURRENT_AASAN))
        {
            mCurrentAasan = getIntent().getParcelableExtra(BaseAasanActivity.KEY_CURRENT_AASAN);
            setupBreakTimeText();
        }
        else
        {
            throw new IllegalArgumentException("Needs current aasan object");
        }
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
                mTimerTextView.setText(schedule.getTimeString());
                initTimer(schedule.duration * 1000L);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                showToast(databaseError.getMessage());
            }
        });
    }

    /**
     * Creates a CountDown timer for given duration.
     *
     * @param timer Timer duration in milliseconds
     */
    private void initTimer(final long timer)
    {
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

    public static void startActivity(Context context, CurrentAasan currentAasan)
    {
        Intent intent = new Intent(context, BreakActivity.class);
        intent.putExtra(BaseAasanActivity.KEY_CURRENT_AASAN, currentAasan);
        context.startActivity(intent);
    }

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
                .setTitle(R.string.dialog_title_stop_pranayama)
                .setPositiveButton(R.string.dialog_action_stop, new DialogInterface.OnClickListener() {
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

        // if last set of current aasan is done, start next aasan
        // else start next set of current aasan
        if (mCurrentAasan.isLastSet())
        {
            // to start first set of next aasan
            mCurrentAasan.setCurrentSet(1);

            Intent intent = new Intent(this, mCurrentAasan.getNextAasanClass());
            intent.putExtra(BaseAasanActivity.KEY_CURRENT_AASAN, mCurrentAasan);
            startActivity(intent);
            finish();
        }
        else
        {
            // move the current set pointer to next one
            mCurrentAasan.incrementCurrentSet();

            Intent intent = new Intent(this, mCurrentAasan.getCurrentAasanClass());
            intent.putExtra(BaseAasanActivity.KEY_CURRENT_AASAN, mCurrentAasan);
            startActivity(intent);
            finish();
        }

    }

}
