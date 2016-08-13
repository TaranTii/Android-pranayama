package it.techies.pranayama.modules.launcher;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseDrawerActivity;
import it.techies.pranayama.modules.aasans.Bhastrika;
import it.techies.pranayama.modules.aasans.model.CurrentAasan;
import it.techies.pranayama.modules.setup.SetupActivity;
import it.techies.pranayama.utils.FireRef;

public class LauncherActivity extends BaseDrawerActivity implements LauncherView {

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

    @Bind(R.id.setup_schedule_ll)
    View mSetupScheduleView;

    @Bind(R.id.aasans_ll)
    View mAasansView;

    @BindDrawable(R.drawable.ic_aasan_active_48dp)
    Drawable mIcAasanActive;

    @BindDrawable(R.drawable.ic_aasan_deactive_48dp)
    Drawable mIcAasanDeActive;

    private LauncherPresenter mPresenter;

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

        if (getUser() == null)
        {
            logoutUser();
        }
        else
        {
            mPresenter = new LauncherPresenterImpl(this, getUser());
            enableFirebaseCache();
        }
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

        prefs.keepSynced(true);
        history.keepSynced(true);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mPresenter.onDestroy();
    }

    @Override
    public void showSetupView(boolean show)
    {
        if (show)
        {
            mAasansView.setVisibility(View.GONE);
            mSetupScheduleView.setVisibility(View.VISIBLE);
        }
        else
        {
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
    }

    @OnClick(R.id.setup_schedule_ll)
    public void setupScheduleClick()
    {
        startActivity(new Intent(this, SetupActivity.class));
    }

}
