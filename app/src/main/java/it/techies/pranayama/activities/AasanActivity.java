package it.techies.pranayama.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
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

public class AasanActivity extends AppCompatActivity
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

    // current aasan index
    private Integer currentAasanIndex;

    // aasan information
    private AasanInformation aasanInformation;

    private CountDownTimer mCountDownTimer;

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
        // TODO: 03/12/2015 handle toggle button action
        Timber.d("Toggle button is checked %b", button.isChecked());

        if(button.isChecked())
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

        Timber.tag(AasanActivity.class.getSimpleName());

        aasanInformation = getIntent().getParcelableExtra(MainActivity.AASAN_LIST_KEY);

        // get current aasan index
        currentAasanIndex = aasanInformation.getCurrentAasanIndex();

        // get current aasan information from aasan list
        AasanTime aasanTime = aasanInformation.getAasanTimes().get(currentAasanIndex);

//        toolbar.setTitle(aasanTime.getName());
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle(aasanTime.getName());

        // count down timer seconds
        int timerSeconds = 0;

        // "00:00:30"
        // parse the aasan time string
        String[] timeArray = aasanTime.getTime().split(":");

        // according to time format array length should be 3
        if (timeArray.length == 3)
        {
            try
            {
                int hours = Integer.valueOf(timeArray[0]);
                int minutes = Integer.valueOf(timeArray[1]);
                int seconds = Integer.valueOf(timeArray[2]);

                timerSeconds = hours * 3600 + minutes * 60 + seconds;
                Timber.d("timer seconds %d", timerSeconds);
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

        mCountDownTimer = new CountDownTimer(timerSeconds * 1000, 1000)
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
                startBreak();
            }
        };

        // start the timer
        mCountDownTimer.start();

    }

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

    private void startFinalScreen()
    {
        // open the final screen
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Toast.makeText(this, "This was your final aasan", Toast.LENGTH_LONG).show();
    }

}
