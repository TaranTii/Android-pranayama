package it.techies.pranayama.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.timing.AasanInformation;
import it.techies.pranayama.api.timing.AasanTime;
import timber.log.Timber;

public class BreakActivity extends AppCompatActivity
{

    @Bind(R.id.timer_tv)
    TextView mTimerTextView;

    private int seconds = 0;

    private int minutes = 0;

    // current aasan index
    private Integer currentAasanIndex;

    // aasan information
    private AasanInformation aasanInformation;

    private CountDownTimer mCountDownTimer;

    @OnClick(R.id.skip_btn)
    public void skipButtonClick(View view)
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

        mCountDownTimer = new CountDownTimer(10 * 1000, 1000)
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
                Timber.d("break timer finished...");
                startNextAasan();
            }
        };

        mCountDownTimer.start();
    }

    private void startNextAasan()
    {
        // get current aasan index
        currentAasanIndex = aasanInformation.getCurrentAasanIndex();

        // update current aasan index
        aasanInformation.setCurrentAasanIndex(currentAasanIndex + 1);

        Intent intent = new Intent(this, AasanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainActivity.AASAN_LIST_KEY, aasanInformation);
        startActivity(intent);
    }

}
