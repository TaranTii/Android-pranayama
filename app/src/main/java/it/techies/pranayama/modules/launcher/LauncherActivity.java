package it.techies.pranayama.modules.launcher;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseDrawerActivity;
import it.techies.pranayama.modules.aasans.Bhastrika;
import it.techies.pranayama.modules.aasans.model.CurrentAasan;
import it.techies.pranayama.modules.history.Aasan;
import it.techies.pranayama.modules.setup.SetupActivity;
import it.techies.pranayama.utils.FireRef;

public class LauncherActivity extends BaseDrawerActivity implements LauncherView {

    private static final String TAG = "LauncherActivity";

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

    @Bind(R.id.setup_schedule_tv)
    View mSetupScheduleView;

    @Bind(R.id.aasans_ll)
    View mAasansView;

    @BindDrawable(R.drawable.ic_aasan_active_48dp)
    Drawable mIcAasanActive;

    @BindDrawable(R.drawable.ic_aasan_de_active_48dp)
    Drawable mIcAasanDeActive;

    @BindDrawable(R.drawable.ic_aasan_v2_active_48dp)
    Drawable mIcAasanV2Active;

    @BindDrawable(R.drawable.ic_aasan_v2_de_active_48dp)
    Drawable mIcAasanV2DeActive;

    private LauncherPresenter mPresenter;

    private ValueEventListener mLastActivityListener;
    private ChildEventListener mLastActivityDetailsListener;
    private DatabaseReference mLastActivityRef;
    private DatabaseReference mLastActivityDetailsRef;

    private int mTotalDuration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setupDrawer(toolbar, savedInstanceState);

        if (getUser() == null) {
            logoutUser();
        } else {
            enableFirebaseCache();
            mPresenter = new LauncherPresenterImpl(this, getUser());
        }

        mLastActivityRef = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_USERS)
                .child(getUid())
                .child(FireRef.REF_USER_LAST_ACTIVITY_DATE);

        mLastActivityDetailsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Log.d(TAG, "onChildAdded() called with: " + "dataSnapshot = [" + dataSnapshot + "], s = [" + s + "]");

                Aasan aasan = dataSnapshot.getValue(Aasan.class);

                // mark aasan completed
                markComplete(aasan.aasanKey);

                // add aasan duration in total time
                updateAasanDuration(aasan.duration, false, false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                Log.d(TAG, "onChildChanged() called with: " + "dataSnapshot = [" + dataSnapshot + "], s = [" + s + "]");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onChildRemoved() called with: " + "dataSnapshot = [" + dataSnapshot + "]");

                Aasan aasan = dataSnapshot.getValue(Aasan.class);

                // mark aasan completed
                markUnComplete(aasan.aasanKey);

                // add aasan duration in total time
                updateAasanDuration(aasan.duration, true, false);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {
                Log.d(TAG, "onChildMoved() called with: " + "dataSnapshot = [" + dataSnapshot + "], s = [" + s + "]");
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled() called with: " + "databaseError = [" + databaseError.getDetails() + "]");
            }
        };

        mLastActivityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange() called with: " + "dataSnapshot = [" + dataSnapshot + "]");

                if (dataSnapshot.exists()) {
                    String date = dataSnapshot.getValue(String.class);

                    mLastActivityDetailsRef = FirebaseDatabase.getInstance()
                            .getReference(FireRef.REF_HISTORY)
                            .child(getUid())
                            .child(date);

                    updateAasanDuration(0, false, true);

                    mLastActivityDetailsRef.addChildEventListener(mLastActivityDetailsListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled() called with: " + "databaseError = [" + databaseError.getDetails() + "]");
            }
        };

        mLastActivityRef.addValueEventListener(mLastActivityListener);
        updateAasanDuration(0, false, true);
    }

    private void enableFirebaseCache()
    {
        // cache user prefs
        DatabaseReference prefs = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_USER_PREFS)
                .child(getUid());

        // cache user history
        DatabaseReference history = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_HISTORY)
                .child(getUid());

        DatabaseReference user = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_USERS)
                .child(getUid());

        prefs.keepSynced(true);
        history.keepSynced(true);
        user.keepSynced(true);
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy() called with: " + "null");

        super.onDestroy();
        mPresenter.onDestroy();

        if (mLastActivityRef != null && mLastActivityListener != null) {
            mLastActivityRef.removeEventListener(mLastActivityListener);
        }

        if (mLastActivityDetailsRef != null && mLastActivityDetailsListener != null) {
            mLastActivityDetailsRef.removeEventListener(mLastActivityDetailsListener);
        }
    }

    @Override
    public void showSetupView(boolean show)
    {
        if (show) {
            mAasansView.setVisibility(View.GONE);
            mSetupScheduleView.setVisibility(View.VISIBLE);
        } else {
            mSetupScheduleView.setVisibility(View.GONE);
            mAasansView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showToastMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.start_button)
    public void start(View v)
    {
        Bhastrika.startActivity(this, new CurrentAasan());
        finish();
    }

    @OnClick(R.id.setup_schedule_tv)
    public void setupScheduleClick(View v)
    {
        startActivity(new Intent(this, SetupActivity.class));
    }

    /**
     * Marks all the aasans as un completed.
     */
    private void markAllUnComplete()
    {
        mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
        mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanV2DeActive, null, null);
        mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
        mBharamri.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
        mBhastrika.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
        mBahaya.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
        mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
    }

    /**
     * Marks the aasan completed.
     *
     * @param aasanKey Aasan key
     */
    private void markComplete(String aasanKey)
    {
        switch (aasanKey) {
            case FireRef.REF_AASAN_AGNISARKRIYA:
                mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                break;
            case FireRef.REF_AASAN_ANULOMVILOM:
                mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanV2Active, null, null);
                break;
            case FireRef.REF_AASAN_KAPALBHATI:
                mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                break;
            case FireRef.REF_AASAN_BHARMARI:
                mBharamri.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                break;
            case FireRef.REF_AASAN_BHASTRIKA:
                mBhastrika.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                break;
            case FireRef.REF_AASAN_BAHAYA:
                mBahaya.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                break;
            case FireRef.REF_AASAN_UDGEETH:
                mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanActive, null, null);
                break;
        }
    }

    /**
     * Marks the aasan as un complete.
     *
     * @param aasanKey Aasan key.
     */
    private void markUnComplete(String aasanKey)
    {
        switch (aasanKey) {
            case FireRef.REF_AASAN_AGNISARKRIYA:
                mAgnisarKriya.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
                break;
            case FireRef.REF_AASAN_ANULOMVILOM:
                mAnulomVilom.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanV2DeActive, null, null);
                break;
            case FireRef.REF_AASAN_KAPALBHATI:
                mKapalBhati.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
                break;
            case FireRef.REF_AASAN_BHARMARI:
                mBharamri.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
                break;
            case FireRef.REF_AASAN_BHASTRIKA:
                mBhastrika.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
                break;
            case FireRef.REF_AASAN_BAHAYA:
                mBahaya.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
                break;
            case FireRef.REF_AASAN_UDGEETH:
                mUdgeeth.setCompoundDrawablesWithIntrinsicBounds(null, mIcAasanDeActive, null, null);
                break;
        }
    }

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

}
