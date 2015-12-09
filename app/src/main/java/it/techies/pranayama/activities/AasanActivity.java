package it.techies.pranayama.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.timing.AasanInformation;
import it.techies.pranayama.api.timing.AasanTime;
import timber.log.Timber;

public class AasanActivity extends BaseActivity
{

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
    private Integer currentAasanIndex;

    /**
     * Prayanama information.
     */
    private AasanInformation aasanInformation;

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

        if (mCountDownTimer != null)
        {
            mCountDownTimer.cancel();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        if (currentAasanIndex == aasanInformation.getAasanTimes().size() - 1)
        {
            startFinalScreen();
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
        aasanInformation = getIntent().getParcelableExtra(MainActivity.AASAN_LIST_KEY);

        // get current aasan index
        currentAasanIndex = aasanInformation.getCurrentAasanIndex();

        // get current aasan information from aasan list
        final AasanTime aasanTime = aasanInformation.getAasanTimes().get(currentAasanIndex);

        // update the title bar with the name of current aasan
        ab.setTitle(aasanTime.getName());

        // read number of sets in pranayama
        currentSet = 1;

        // total sets in current aasan
        totalSets = aasanTime.getSet();

        // update the current aasan and total aasan on screen
        mSetTextView.setText(String.format("%d of %d", currentSet, totalSets));

        // parse the aasan time string, i.e "00:00:30"
        String[] timeArray = aasanTime.getTime().split(":");

        // according to time format array length should be 3
        if (timeArray.length == 3)
        {
            try
            {
                long hours = Long.valueOf(timeArray[0]);
                long minutes = Long.valueOf(timeArray[1]);
                long seconds = Long.valueOf(timeArray[2]);

                mSingleSetDuration = (hours * 3600 + minutes * 60 + seconds) * 1000;
                createTimer(mSingleSetDuration);
            }
            catch (NumberFormatException e)
            {
                Timber.e(e, "NumberFormatException");
                Toast.makeText(this, "Unable to read aasan time", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else
        {
            Toast.makeText(this, "Unable to read aasan time", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void startTimer()
    {
        mTimerTextView.setText(String.format(
                "%02d:%02d",
                (timerSeconds / 60000) % 60,
                (timerSeconds / (60 * 1000) % 60) )
        );

        mCountDownTimer = new CountDownTimer(timerSeconds, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                Timber.d("millisUntilFinished %02d", millisUntilFinished);
                timerSeconds = millisUntilFinished;

                long seconds = millisUntilFinished / 1000 % 60;
                long minutes = millisUntilFinished / (60 * 1000) % 60;

                mTimerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish()
            {
                Timber.d("aasan timer finished...");
                mTimerTextView.setText(String.format("%02d:%02d", 0, 0));

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

    private void createTimer(long seconds)
    {
        timerSeconds = seconds;
        startTimer();
    }

    private void resumeTimer()
    {
        startTimer();
    }

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
        // get current aasan index
        currentAasanIndex = aasanInformation.getCurrentAasanIndex();

        // check if this aasan is the last one
        if (currentAasanIndex + 1 == aasanInformation.getAasanTimes().size())
        {
            startFinalScreen();
        }
        else
        {
            Intent intent = new Intent(this, BreakActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(MainActivity.AASAN_LIST_KEY, aasanInformation);
            startActivity(intent);
        }
    }

    /**
     * Show the final screen after completing the Prayanama.
     */
    private void startFinalScreen()
    {
        // open the final screen
        Intent intent = new Intent(this, EndActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
