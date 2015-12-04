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

    private int seconds = 0;

    private int minutes = 0;

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
            mCountDownTimer.cancel();
        }
        else
        {
            // pause the timer
            mCountDownTimer.start();
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

                createTimer(hours * 3600 + minutes * 60 + seconds);
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
        mCountDownTimer = new CountDownTimer(timerSeconds * 1000 + 1000, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                String mSeconds = "";
                String mMinutes = "";

                seconds++;

                if (seconds > 59)
                {
                    seconds = 0;
                    minutes++;
                }

                timerSeconds = millisUntilFinished;

                Timber.d("seconds Until Finished %d", millisUntilFinished / 1000);

                if (seconds < 10)
                {
                    mSeconds = "0" + seconds;
                }
                else
                {
                    mSeconds = String.valueOf(seconds);
                }

                if (minutes < 10)
                {
                    mMinutes = "0" + minutes;
                }
                else
                {
                    mMinutes = String.valueOf(minutes);
                }

                mTimerTextView.setText(String.format("%s:%s", mMinutes, mSeconds));
            }

            @Override
            public void onFinish()
            {
                Timber.d("aasan timer finished...");

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

    private void pauseTimer()
    {

    }

    /**
     * Start the next set of aasan.
     */
    private void startNextSet()
    {
        // set counter minutes and seconds to zero
        seconds = 0;
        minutes = 0;

        // update current set
        currentSet++;

        // update the current set counter on screen
        mSetTextView.setText(String.format("%d of %d", currentSet, totalSets));

        // restart the counter
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
        if (currentAasanIndex < aasanInformation.getAasanTimes().size())
        {
            Intent intent = new Intent(this, BreakActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(MainActivity.AASAN_LIST_KEY, aasanInformation);
            startActivity(intent);
        }
        else
        {
            startFinalScreen();
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
