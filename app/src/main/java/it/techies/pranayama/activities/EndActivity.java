package it.techies.pranayama.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.DailyRoutine;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.infrastructure.OnResetTokenSuccessCallBack;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class EndActivity extends BaseActivity {

    @OnClick(R.id.home_btn)
    public void homeButtonClick(View v)
    {
        // open the final screen
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.share_btn)
    public void shareButtonClick(View v)
    {
        Timber.d("Sharing...");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sendDailyRouting();
    }

    private void sendDailyRouting()
    {
        DailyRoutine dailyRoutine = getIntent().getParcelableExtra(MainActivity.DAILY_ROUTINE_KEY);

        List<DailyRoutine> dailyRoutineList = new ArrayList<>();
        dailyRoutineList.add(dailyRoutine);

        Call<EmptyResponse> call = mApiClient.setDailyRoutine(dailyRoutineList);
        call.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    Timber.d("isSuccess()");
                }
                else
                {
                    int statusCode = response.code();
                    if (statusCode == 403)
                    {
                        // reset token
                        resetToken(new OnResetTokenSuccessCallBack() {
                            @Override
                            public void onSuccess(String token)
                            {
                                mAuth.setToken(EndActivity.this, token);
                                sendDailyRouting();
                            }
                        });
                    }
                    else
                    {
                        Timber.d("Status code %d", statusCode);
                    }

                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                onRetrofitFailure(t);
            }
        });
    }

}
