package it.techies.pranayama.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.facebook.appevents.AppEventsLogger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.AasanNames;
import it.techies.pranayama.api.DailyRoutine;
import it.techies.pranayama.api.history.Aasan;
import it.techies.pranayama.api.history.HistoryRequest;
import it.techies.pranayama.api.timing.AasanInformation;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.infrastructure.BaseDrawerActivity;
import it.techies.pranayama.infrastructure.OnResetTokenSuccessCallBack;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class LauncherActivity extends BaseDrawerActivity {

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

    @BindDrawable(R.drawable.ic_aasan_active_48dp)
    Drawable mIcAasanActive;

    private ArrayList<AasanTime> aasanTimes;

    public static final String AASAN_LIST_KEY = "AASAN_LIST_KEY";
    public static final String DAILY_ROUTINE_KEY = "DAILY_ROUTINE_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDrawer(toolbar, savedInstanceState);
        init();
    }

    private void init()
    {
        Timber.tag(LauncherActivity.class.getSimpleName());

        // if user has mHistory this will be 1 otherwise 0
        Integer mHistory = getIntent().getIntExtra(LoginActivity.USER_HISTORY, -1);

        // start background music service
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        showLoadingDialog("Loading...");

        getHistory();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @OnClick(R.id.start_button)
    public void start(View v)
    {
        if (aasanTimes != null && aasanTimes.size() != 0)
        {
            Intent intent = new Intent(this, AasanActivity.class);

            int currentAasanIndex = 0;
            AasanInformation aasanInformation = new AasanInformation(currentAasanIndex, aasanTimes);
            DailyRoutine dailyRoutine = new DailyRoutine();

            intent.putExtra(AASAN_LIST_KEY, aasanInformation);
            intent.putExtra(DAILY_ROUTINE_KEY, dailyRoutine);

            startActivity(intent);
            finish();
        }
        else
        {
            Timber.d("aasan times is zero or null");
        }
    }

    private void getHistory()
    {
        Call<List<Aasan>> call = mApiClient.getHistory(new HistoryRequest(""));

        call.enqueue(new Callback<List<Aasan>>() {
            @Override
            public void onResponse(Response<List<Aasan>> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    // get aasan timing
                    getAasanTiming();

                    List<Aasan> aasans = response.body();

                    if (aasans == null)
                    {
                        return;
                    }

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
                        resetToken(new OnResetTokenSuccessCallBack() {
                            @Override
                            public void onSuccess(String token)
                            {
                                mAuth.setToken(LauncherActivity.this, token);
                                getHistory();
                            }
                        });
                    }
                    else
                    {
                        Utils.hideLoadingDialog(mDialog);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.e(t, "getHistory");
                Utils.hideLoadingDialog(mDialog);
                Utils.handleRetrofitFailure(LauncherActivity.this, t);
            }
        });
    }

    private void getAasanTiming()
    {
        Call<ArrayList<AasanTime>> call = mApiClient.getAasanTiming();
        call.enqueue(new Callback<ArrayList<AasanTime>>() {
            @Override
            public void onResponse(Response<ArrayList<AasanTime>> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    hideLoadingDialog();
                    aasanTimes = response.body();
                    if (aasanTimes != null)
                    {
                        Timber.d("aasan times size %d ", aasanTimes.size());
                    }
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
                                mAuth.setToken(LauncherActivity.this, token);
                                getAasanTiming();
                            }
                        });
                    }
                    else
                    {
                        Utils.hideLoadingDialog(mDialog);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.e(t, "getAasanTiming");
                hideLoadingDialog();
                onRetrofitFailure(t);
            }
        });
    }

    /**
     * Read the meta data of the history.
     *
     * @param aasan Aasan information
     */
    private void readMeta(Aasan aasan)
    {
        // "date": "2015-09-03",
        // "gmt": "IST",
        // "time": "00:00:00",
        // "time_zone": "Asia/Kolkata"

        String time = aasan.getTime();
        String[] times = time.split(":");

        if (times.length == 3)
        {
            if (times[0].equals("00"))
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
                    mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                }
                break;
            case AasanNames.Anulom_Vilom:
                if (isCompleted == 1)
                {
                    mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                }
                break;
            case AasanNames.Kapalbhati:
                if (isCompleted == 1)
                {
                    mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                }
                break;
            case AasanNames.Bharmari:
                if (isCompleted == 1)
                {
                    mBharamri.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                }
                break;
            case AasanNames.Bhastrika:
                if (isCompleted == 1)
                {
                    mBhastrika.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                }
                break;
            case AasanNames.Bahi:
                if (isCompleted == 1)
                {
                    mBahaya.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                }
                break;
            case AasanNames.Udgeeth:
                if (isCompleted == 1)
                {
                    mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                }
                break;
        }
    }


}
