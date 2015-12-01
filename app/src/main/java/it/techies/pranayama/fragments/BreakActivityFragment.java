package it.techies.pranayama.fragments;

import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class BreakActivityFragment extends Fragment
{

    @Bind(R.id.textView)
    TextView mTextView;

    int seconds = 0;
    int minutes = 0;

    public BreakActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_break, container, false);

        ButterKnife.bind(this, view);

        mCountDownTimer.start();

        return view;
    }

    CountDownTimer mCountDownTimer = new CountDownTimer(70000, 1000)
    {
        @Override
        public void onTick(long millisUntilFinished)
        {
            String mSeconds = "";
            String mMinutes = "";

            seconds++;

            if(seconds > 59)
            {
                seconds = 0;
                minutes++;
            }

            Timber.d("millisUntilFinished %d", millisUntilFinished);

            if(seconds < 10)
            {
                mSeconds = "0" + seconds;
            }
            else
            {
                mSeconds = String.valueOf(seconds);
            }

            if(minutes < 10)
            {
                mMinutes = "0" + minutes;
            }
            else
            {
                mMinutes = String.valueOf(minutes);
            }

            mTextView.setText(String.format("%s:%s", mMinutes, mSeconds));
        }

        @Override
        public void onFinish()
        {
            Timber.d("timer finished...");
        }
    };
}
