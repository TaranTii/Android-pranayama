package it.techies.pranayama.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.history.Aasan;
import it.techies.pranayama.api.history.HistoryRequest;
import it.techies.pranayama.api.token.ResetTokenCallBack;
import it.techies.pranayama.utils.SessionStorage;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class HistoryActivity extends AppCompatActivity
{

    @Bind(R.id.calendarView)
    MaterialCalendarView mCalendarView;

    private ApiClient.ApiInterface mApiClient;

    private SessionStorage mSessionStorage;

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

        mSessionStorage = new SessionStorage(this);
        String email = mSessionStorage.getEmail();
        String token = mSessionStorage.getAccessToken();
        mApiClient = ApiClient.getApiClient(email, token);

        mCalendarView.setCurrentDate(new Date());
        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener()
        {
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

        call.enqueue(new Callback<List<Aasan>>()
        {
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
                        final String email = mSessionStorage.getEmail();
                        String token = mSessionStorage.getAccessToken();

                        Utils.resetToken(getApplicationContext(), mApiClient, email, token, new ResetTokenCallBack()
                        {
                            @Override
                            public void onSuccess(String token)
                            {
                                Timber.d("resetToken : onSuccess()");

                                mSessionStorage.setAccessToken(token);
                                mApiClient = ApiClient.getApiClient(email, token);

                                getHistory(date);
                            }
                        });
                    }
                    else if(statusCode == 422)
                    {
                        Toast.makeText(getApplicationContext(), "History was not found for selected date", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.e(t, "getHistory");
                Utils.handleRetrofitFailure(getApplicationContext(), t);
            }
        });
    }

}
