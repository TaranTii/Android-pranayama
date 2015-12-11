package it.techies.pranayama.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.api.token.ResetTokenCallBack;
import it.techies.pranayama.utils.SessionStorage;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SetupActivity extends AppCompatActivity
{

    @Bind(R.id.aasan_timing_lv)
    ListView mAasanTimingListView;

    private ApiClient.ApiInterface mApiClient;

    private SessionStorage mSessionStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);

        mSessionStorage = new SessionStorage(this);
        String email = mSessionStorage.getEmail();
        String token = mSessionStorage.getAccessToken();

        mApiClient = ApiClient.getApiClient(email, token);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setPranayamaTiming(final List<AasanTime> aasanTimeList)
    {
        Call<EmptyResponse> call = mApiClient.setPranayamaTiming(aasanTimeList);
        call.enqueue(new Callback<EmptyResponse>()
        {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    Utils.showToast(getApplicationContext(), "Saved...");
                }
                else
                {
                    int statusCode = response.code();
                    Timber.d("[Err] could not set pranayama timing, statusCode %d", statusCode);

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

                                setPranayamaTiming(aasanTimeList);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Utils.handleRetrofitFailure(getApplicationContext(), t);
            }
        });
    }

    private void getAasanTiming()
    {
        Call<ArrayList<AasanTime>> call = mApiClient.getAasanTiming();
        call.enqueue(new Callback<ArrayList<AasanTime>>()
        {
            @Override
            public void onResponse(Response<ArrayList<AasanTime>> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    List<AasanTime> aasanTimes = response.body();
                    Timber.d("aasan times size %d ", aasanTimes.size());
                }
                else
                {
                    int statusCode = response.code();
                    Timber.d("[Err] could not get aasan timing, statusCode %d", statusCode);

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
                Utils.handleRetrofitFailure(getApplicationContext(), t);
            }
        });
    }
}
