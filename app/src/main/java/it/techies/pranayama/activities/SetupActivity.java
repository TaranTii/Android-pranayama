package it.techies.pranayama.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.adapters.AasanListAdapter;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.infrastructure.OnResetTokenSuccessCallBack;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SetupActivity extends BaseActivity {

    @Bind(R.id.aasan_timing_lv)
    ListView mAasanTimingListView;

    @Bind(R.id.loading_ll)
    View mLoadingVIew;

    @Bind(R.id.reload_ll)
    View mReloadView;

    private AasanListAdapter adapter;

    @OnClick(R.id.reload_btn)
    public void reload(View v)
    {
        Timber.d("reload btn...");
        showReload(false);
        getAasanTiming();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new AasanListAdapter(this, new ArrayList<AasanTime>());
        mAasanTimingListView.setAdapter(adapter);

        getAasanTiming();
    }

    private void showLoading(boolean state)
    {
        mAasanTimingListView.setVisibility(state ? View.GONE : View.VISIBLE);
        mLoadingVIew.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private void showReload(boolean state)
    {
        mAasanTimingListView.setVisibility(state ? View.GONE : View.VISIBLE);
        mReloadView.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private void setPranayamaTiming(final List<AasanTime> aasanTimeList)
    {
        Call<EmptyResponse> call = mApiClient.setPranayamaTiming(aasanTimeList);
        call.enqueue(new Callback<EmptyResponse>() {
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
                        resetToken(new OnResetTokenSuccessCallBack() {
                            @Override
                            public void onSuccess(String token)
                            {
                                mAuth.setToken(SetupActivity.this, token);
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
        showLoading(true);

        Call<ArrayList<AasanTime>> call = mApiClient.getAasanTiming();
        call.enqueue(new Callback<ArrayList<AasanTime>>() {
            @Override
            public void onResponse(Response<ArrayList<AasanTime>> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    showLoading(false);

                    List<AasanTime> aasanTimes = response.body();
                    Timber.d("aasan times size %d ", aasanTimes.size());
                    adapter.addAll(aasanTimes);
                }
                else
                {

                    int statusCode = response.code();
                    Timber.d("[Err] could not get aasan timing, statusCode %d", statusCode);

                    if (statusCode == 401)
                    {
                        resetToken(new OnResetTokenSuccessCallBack() {
                            @Override
                            public void onSuccess(String token)
                            {
                                mAuth.setToken(SetupActivity.this, token);
                                getAasanTiming();
                            }
                        });
                    }
                    else
                    {
                        showLoading(false);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                showLoading(false);
                showReload(true);

                Timber.e(t, "getAasanTiming");
                Utils.handleRetrofitFailure(getApplicationContext(), t);
            }
        });
    }
}
