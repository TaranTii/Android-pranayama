package it.techies.pranayama.modules.history;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.utils.FireRef;
import it.techies.pranayama.utils.Utils;

public class HistoryActivity extends BaseActivity {

    private static final String TAG = "HistoryActivity";

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

    @BindDrawable(R.drawable.ic_aasan_active_24dp)
    Drawable mIcAasanActive;

    @BindDrawable(R.drawable.ic_aasan_de_active_24dp)
    Drawable mIcAasanDeActive;

    ChildEventListener mChildEventListener;
    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected)
            {
                Log.d(TAG, "onDateSelected: " + Utils.getDateFromCalender(date.getCalendar()));

                updateAasanDuration(0, false, true);
                showSelectedDate();
                getHistory(Utils.getDateFromCalender(date.getCalendar()));
            }
        });

        mCalendarView.setCurrentDate(new Date());
        mCalendarView.setDateSelected(new Date(), true);

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Aasan aasan = dataSnapshot.getValue(Aasan.class);
                Log.d(TAG, "onChildAdded: Aasan: " + aasan.toString());

                // mark aasan completed
                markComplete(aasan);

                // add aasan duration in total time
                updateAasanDuration(aasan.duration, false, false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                Aasan aasan = dataSnapshot.getValue(Aasan.class);
                Log.d(TAG, "onChildRemoved: Aasan: " + aasan.toString());

                // mark aasan un complete
                markUnComplete(aasan);

                // remove aasan duration from total time
                updateAasanDuration(aasan.duration, true, false);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled() called with: " + databaseError.getDetails());
            }
        };

        showSelectedDate();
        updateAasanDuration(0, false, true);

        getHistory(Utils.getCurrentDate());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mDatabaseReference != null && mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    private void getHistory(final String date)
    {
        if (mDatabaseReference != null && mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }

        Log.d(TAG, "getHistory() called with: " + "date = [" + date + "]");

        markAllUnComplete();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference(FireRef.REF_HISTORY).child(getUid()).child(date);

        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    /**
     * Marks all the aasans as un completed.
     */
    private void markAllUnComplete()
    {
        mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
        mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
        mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
        mBharamri.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
        mBhastrika.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
        mBahaya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
        mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
    }

    /**
     * Marks the aasan completed.
     *
     * @param aasan Aasan information
     */
    private void markComplete(Aasan aasan)
    {
        switch (aasan.aasanKey) {
            case FireRef.REF_AASAN_AGNISARKRIYA:
                mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                break;
            case FireRef.REF_AASAN_ANULOMVILOM:
                mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                break;
            case FireRef.REF_AASAN_KAPALBHATI:
                mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                break;
            case FireRef.REF_AASAN_BHARMARI:
                mBharamri.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                break;
            case FireRef.REF_AASAN_BHASTRIKA:
                mBhastrika.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                break;
            case FireRef.REF_AASAN_BAHAYA:
                mBahaya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                break;
            case FireRef.REF_AASAN_UDGEETH:
                mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(mIcAasanActive, null, null, null);
                break;
        }
    }

    /**
     * Marks the aasan as un complete.
     *
     * @param aasan Aasan information.
     */
    private void markUnComplete(Aasan aasan)
    {
        switch (aasan.aasanKey) {
            case FireRef.REF_AASAN_AGNISARKRIYA:
                mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                break;
            case FireRef.REF_AASAN_ANULOMVILOM:
                mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                break;
            case FireRef.REF_AASAN_KAPALBHATI:
                mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                break;
            case FireRef.REF_AASAN_BHARMARI:
                mBharamri.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                break;
            case FireRef.REF_AASAN_BHASTRIKA:
                mBhastrika.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                break;
            case FireRef.REF_AASAN_BAHAYA:
                mBahaya.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                break;
            case FireRef.REF_AASAN_UDGEETH:
                mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(mIcAasanDeActive, null, null, null);
                break;
        }
    }

    private int mTotalDuration = 0;

    /**
     * Calculated the total aasan duration and updates the TimeView.
     */
    private void updateAasanDuration(int duration, boolean remove, boolean reset)
    {
        if (reset) {
            mTotalDuration = 0;
        }

        if (remove) {
            mTotalDuration -= duration;
        } else {
            mTotalDuration += duration;
        }


        String time = String.format(Locale.getDefault(), "%02dm %02ds", mTotalDuration / 60, mTotalDuration % 60);

        mTimeTextView.setText(time);
    }

    /**
     * Shows selected date in calendar.
     */
    private void showSelectedDate()
    {
        CalendarDay mCalender = mCalendarView.getSelectedDate();

        SimpleDateFormat dateFormat0 = new SimpleDateFormat("d", Locale.getDefault());
        mDayTextView.setText(dateFormat0.format(mCalender.getDate()));

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEEE", Locale.getDefault());
        mDayTextTextView.setText(dateFormat1.format(mCalender.getDate()));

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("LLL yyyy", Locale.getDefault());
        mMonthYearTextView.setText(dateFormat2.format(mCalender.getDate()));
    }

}
