package it.techies.pranayama.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.api.AasanNames;
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

    @Bind(R.id.time_tv)
    TextView mTimeTextView;

    @Bind(R.id.day_tv)
    TextView mDayTextView;

    @Bind(R.id.day_text_tv)
    TextView mDayTextTextView;

    @Bind(R.id.month_year_tv)
    TextView mMonthYearTextView;

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

    @Bind(R.id.progress_rl)
    View mProgressView;

    @Bind(R.id.meta_ll)
    View mMetaView;

    @BindDrawable(R.drawable.ic_aasan_active_24dp)
    Drawable mIcAasanActive;

    @BindDrawable(R.drawable.ic_aasan_deactive_24dp)
    Drawable mIcAasanDeActive;

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

        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, CalendarDay date,
                                       boolean selected)
            {

                int day = date.getCalendar().get(Calendar.DAY_OF_MONTH);
                int month = date.getCalendar().get(Calendar.MONTH) + 1;
                int year = date.getCalendar().get(Calendar.YEAR);

                String mDate = String.format("%d-%02d-%02d", year, month, day);

                Timber.d("Date: %s", mDate);
                getHistory(mDate);
            }
        });

        mCalendarView.setCurrentDate(new Date());
        mCalendarView.setDateSelected(new Date(), true);

        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);

        String mDate = String.format("%d-%02d-%02d", year, month, day);
        getHistory(mDate);
    }

    public void showProgress(boolean state)
    {
        mProgressView.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    public void showHistory(boolean state)
    {
        mMetaView.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private void getHistory(final String date)
    {
        showProgress(true);
        showHistory(false);

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
                            Timber.d("Name: %s, isCompleted: %d", aasan.getName(), aasan.getIsCompleted());
                            readAasan(aasan);
                        }
                        else
                        {
                            // this is meta
                            readMeta(aasan);
                        }
                    }

                    // show history
                    showProgress(false);
                    showHistory(true);
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

                        // hide progress and history
                        showProgress(false);
                        showHistory(false);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.e(t, "getHistory");
                onRetrofitFailure(t);

                showProgress(false);
                showHistory(false);
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
            mTimeTextView.setText(String.format("%s:%s mins", times[1], times[2]));

//            if (times[0].equals("00"))
//            {
//            }
//            else
//            {
//                mTimeTextView.setText(String.format("%s mins", time));
//            }
        }

        CalendarDay mCalender = mCalendarView.getSelectedDate();

        SimpleDateFormat dateFormat0 = new SimpleDateFormat("d", Locale.getDefault());
        mDayTextView.setText(dateFormat0.format(mCalender.getDate()));

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEEE", Locale.getDefault());
        mDayTextTextView.setText(dateFormat1.format(mCalender.getDate()));

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("LLL yyyy", Locale.getDefault());
        mMonthYearTextView.setText(dateFormat2.format(mCalender.getDate()));
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
                    mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                }
                else
                {
                    mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                }
                break;
            case AasanNames.Anulom_Vilom:
                if (isCompleted == 1)
                {
                    mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                }
                else
                {
                    mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                }
                break;
            case AasanNames.Kapalbhati:
                if (isCompleted == 1)
                {
                    mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                }
                else
                {
                    mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                }
                break;
            case AasanNames.Bharmari:
                if (isCompleted == 1)
                {
                    mBharamri.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                }
                else
                {
                    mBharamri.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                }
                break;
            case AasanNames.Bhastrika:
                if (isCompleted == 1)
                {
                    mBhastrika.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                }
                else
                {
                    mBhastrika.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                }
                break;
            case AasanNames.Bahaya:
                if (isCompleted == 1)
                {
                    mBahaya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                }
                else
                {
                    mBahaya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                }
                break;
            case AasanNames.Udgeeth:
                if (isCompleted == 1)
                {
                    mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                }
                else
                {
                    mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                }
                break;
        }
    }

}
