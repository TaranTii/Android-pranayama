package it.techies.pranayama.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.api.history.Aasan;
import it.techies.pranayama.api.history.HistoryRequest;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.infrastructure.OnResetTokenSuccessCallBack;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class HistoryActivity extends BaseActivity {

    @Bind(R.id.calendarView)
    MaterialCalendarView mCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        Timber.tag(HistoryActivity.class.getSimpleName());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCalendarView.setCurrentDate(new Date());
        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, CalendarDay date,
                                       boolean selected)
            {
                int day = date.getDay();
                int month = date.getMonth();
                int year = date.getYear();

                String mDate = String.format("%d-%02d-%02d", year, month, day);

                Timber.d("Date: %s", mDate);
                getHistory(mDate);
            }
        });
    }

    private void getHistory(final String date)
    {
        Call<List<Aasan>> call = mApiClient.getHistory(new HistoryRequest(date));

        call.enqueue(new Callback<List<Aasan>>() {
            @Override
            public void onResponse(Response<List<Aasan>> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    List<Aasan> aasans = response.body();

                    for (Aasan aasan : aasans)
                    {
                        if (aasan.getName() != null)
                        {
                            // this is an aasan
                            // readAasan(aasan);
                            Timber.d(aasan.getName());
                        }
                        else
                        {
                            // this is meta
                            // readMeta(aasan);
                        }
                    }
                }
                else
                {
                    int statusCode = response.code();
                    Timber.d("[Err] could not get history, statusCode %d", statusCode);

                    if (statusCode == 401)
                    {
                        resetToken(new OnResetTokenSuccessCallBack() {
                            @Override
                            public void onSuccess(String token)
                            {
                                mAuth.setToken(HistoryActivity.this, token);
                                getHistory(date);
                            }
                        });
                    }
                    else if (statusCode == 422)
                    {
                        showToast("History was not found for selected date");
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.e(t, "getHistory");
                onRetrofitFailure(t);
            }
        });
    }

}
