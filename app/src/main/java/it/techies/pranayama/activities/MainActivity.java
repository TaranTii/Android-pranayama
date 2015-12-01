package it.techies.pranayama.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.AasanNames;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.history.Aasan;
import it.techies.pranayama.api.history.HistoryRequest;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.api.token.ResetTokenCallBack;
import it.techies.pranayama.utils.SessionStorage;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
{
    @Bind(R.id.time_tv)
    TextView mTimeTextView;

    @Bind(R.id.textView1)
    TextView mBhastrika;

    @Bind(R.id.textView2)
    TextView mKapalBhati;

    @Bind(R.id.textView3)
    TextView mBahaya;

    @Bind(R.id.textView4)
    TextView mAgnisarKriya;

    @Bind(R.id.textView5)
    TextView mAnulomVilom;

    @Bind(R.id.textView6)
    TextView mBharamri;

    @Bind(R.id.textView7)
    TextView mUdgeeth;

    private Integer mHistory;

    private SessionStorage sessionStorage;

    private Context mContext = this;

    private ApiClient.ApiInterface apiClient;

    private List<AasanTime> aasanTimes;

    @OnClick(R.id.start_button)
    public void start(View v)
    {
        startActivity(new Intent(this, BreakActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // if user has mHistory this will be 1 otherwise 0
        mHistory = getIntent().getIntExtra(LoginActivity.USER_HISTORY, -1);

        sessionStorage = new SessionStorage(this);
        String email = sessionStorage.getEmail();
        String token = sessionStorage.getAccessToken();

        apiClient = ApiClient.getApiClient(email, token);

        // read history
        getHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            Timber.d("open settings...");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getHistory()
    {
        Call<List<Aasan>> call = apiClient.getHistory(new HistoryRequest(""));

        call.enqueue(new Callback<List<Aasan>>()
        {
            @Override
            public void onResponse(Response<List<Aasan>> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    // get aasan timing
                    getAasanTiming();

                    List<Aasan> aasans = response.body();

                    for (Aasan aasan : aasans)
                    {
                        if (aasan.getName() != null)
                        {
                            // this is an aasan
                            readAasan(aasan);
                        }
                        else
                        {
                            // this is meta
                            readMeta(aasan);
                        }
                    }
                }
                else
                {
                    int statusCode = response.code();
                    Timber.d("[Err] could not get mHistory, statusCode %d", statusCode);

                    if (statusCode == 401)
                    {
                        final String email = sessionStorage.getEmail();
                        String token = sessionStorage.getAccessToken();

                        Utils.resetToken(mContext, apiClient, email, token, new ResetTokenCallBack()
                        {
                            @Override
                            public void onSuccess(String token)
                            {
                                Timber.d("resetToken : onSuccess()");

                                sessionStorage.setAccessToken(token);
                                apiClient = ApiClient.getApiClient(email, token);

                                getHistory();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.e(t, "getHistory");
            }
        });
    }

    private void getAasanTiming()
    {
        Call<List<AasanTime>> call = apiClient.getAasanTiming();
        call.enqueue(new Callback<List<AasanTime>>()
        {
            @Override
            public void onResponse(Response<List<AasanTime>> response, Retrofit retrofit)
            {
                if(response.isSuccess())
                {
                    aasanTimes = response.body();
                }
                else
                {
                    int statusCode = response.code();
                    Timber.d("[Err] could not get aasan timing, statusCode %d", statusCode);

                    if (statusCode == 401)
                    {
                        final String email = sessionStorage.getEmail();
                        String token = sessionStorage.getAccessToken();

                        Utils.resetToken(mContext, apiClient, email, token, new ResetTokenCallBack()
                        {
                            @Override
                            public void onSuccess(String token)
                            {
                                Timber.d("resetToken : onSuccess()");

                                sessionStorage.setAccessToken(token);
                                apiClient = ApiClient.getApiClient(email, token);

                                getAasanTiming();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.e(t, "getAasanTiming");
            }
        });
    }

    /**
     * Read the meta data of the history.
     *
     * @param aasan
     */
    private void readMeta(Aasan aasan)
    {
//        "date": "2015-09-03",
//            "gmt": "IST",
//            "time": "00:00:00",
//            "time_zone": "Asia/Kolkata"

        String time = aasan.getTime();
        String[] times = time.split(":");

        if(times.length == 3)
        {
            if(times[0].equals("00"))
            {
                mTimeTextView.setText(String.format("%s:%s mins", times[1], times[2]));
            }
            else
            {
                mTimeTextView.setText(String.format("%s mins", time));
            }
        }
    }

    /**
     * Read the aasan details from history.
     *
     * @param aasan
     */
    private void readAasan(Aasan aasan)
    {
        String name = aasan.getName();
        int isCompleted = aasan.getIsCompleted();

        switch (name)
        {
            case AasanNames.Agnisar_Kriya:
                if (isCompleted == 1)
                {
                    mAgnisarKriya.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case AasanNames.Anulom_Vilom:
                if (isCompleted == 1)
                {
                    mAnulomVilom.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case AasanNames.Kapalbhati:
                if (isCompleted == 1)
                {
                    mKapalBhati.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case AasanNames.Bharmari:
                if (isCompleted == 1)
                {
                    mBharamri.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case AasanNames.Bhastrika:
                if (isCompleted == 1)
                {
                    mBhastrika.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case AasanNames.Bahi:
                if (isCompleted == 1)
                {
                    mBahaya.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case AasanNames.Udgeeth:
                if (isCompleted == 1)
                {
                    mUdgeeth.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
        }
    }
}
