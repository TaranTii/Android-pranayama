package it.techies.pranayama.modules.setup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.adapters.AasanListAdapter;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.api.timing.Timings;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.infrastructure.OnResetTokenSuccessCallBack;
import it.techies.pranayama.models.FirebaseSchedule;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SetupActivity extends BaseActivity implements SetupView {

    @Bind(R.id.aasan_timing_lv)
    ListView mAasanTimingListView;

    @Bind(R.id.loading_ll)
    View mLoadingVIew;

    @Bind(R.id.reload_ll)
    View mReloadView;

    private boolean mDidUserMadeChanges = false;
    private boolean mDidUserSavedChanges = false;

    private FirebaseRecyclerAdapter<FirebaseSchedule, SetupViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

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

        mRecycler = (RecyclerView) findViewById(R.id.aasan_timing_rv);

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

    private void getAasanTiming()
    {
        showLoading(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mManager);

        final String uid = getUid();

        // Set up FirebaseRecyclerAdapter with the Query
        Query query = FirebaseDatabase.getInstance().getReference("prefs")
                .child(uid).orderByChild("order");

        mAdapter = new FirebaseRecyclerAdapter<FirebaseSchedule, SetupViewHolder>(
                FirebaseSchedule.class,
                R.layout.list_row_aasan_time,
                SetupViewHolder.class,
                query
        ) {

            @Override
            protected void populateViewHolder(
                    final SetupViewHolder viewHolder,
                    final FirebaseSchedule model,
                    final int position
            )
            {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String key = postRef.getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        // picker(model, position);
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView)
                    {
                        //
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

//    public void picker(final FirebaseSchedule aasanTime, final int position)
//    {
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_aasan_setup, null);
//
//        View mSetLinearLayout = dialogView.findViewById(R.id.set_ll);
//        final NumberPicker sets = (NumberPicker) dialogView.findViewById(R.id.set_np);
//
//        Timings timings;
//
//        // hide mSets in case of break
//        if (position == 7)
//        {
//            mSetLinearLayout.setVisibility(View.GONE);
//
//            timings = new Timings("00:00:00");
//            try
//            {
//                timings.addSeconds(aasanTime.getBreakTime());
//            } catch (NumberFormatException e)
//            {
//                e.printStackTrace();
//                return;
//            }
//        }
//        else
//        {
//            mSetLinearLayout.setVisibility(View.VISIBLE);
//
//            sets.setMinValue(1);
//            sets.setMaxValue(10);
//            sets.setValue(aasanTime.getSet());
//
//            timings = aasanTime.getTimings();
//        }
//
//        final NumberPicker minutes = (NumberPicker) dialogView.findViewById(R.id.minute_np);
//        minutes.setMinValue(0);
//        minutes.setMaxValue(15);
//        minutes.setValue((int) timings.getMinutes());
//
//        Timber.d("Minutes %d, s %d", (int) timings.getMinutes(), (int) timings.getSeconds());
//
//        final NumberPicker seconds = (NumberPicker) dialogView.findViewById(R.id.second_np);
//        seconds.setMinValue(0);
//        seconds.setMaxValue(59);
//        seconds.setValue((int) timings.getSeconds());
//
//        new AlertDialog.Builder(this)
//                .setView(dialogView)
//                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//                        mDidUserMadeChanges = true;
//
//                        int selectedMinutes = minutes.getValue();
//                        int selectedSeconds = seconds.getValue();
//                        int selectedSets = sets.getValue();
//
//                        if (selectedMinutes == 0 && selectedSeconds < 15)
//                        {
//                            showMinimumTimeDialog();
//                            return;
//                        }
//
//                        String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", 0, selectedMinutes, selectedSeconds);
//
//                        if (position == 7)
//                        {
//                            Timings breakTimings = new Timings(time);
//                            adapter.setBreakTime(breakTimings.getTotalTimeInSeconds());
//                        }
//                        else
//                        {
//                            aasanTime.setSet(selectedSets);
//                            aasanTime.setTime(time);
//                            adapter.notifyDataSetChanged();
//                        }
//
//                    }
//                })
//                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//
//                    }
//                })
//                .show();
//    }

    private void showMinimumTimeDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Aasan/Break time should be more than 14 seconds")
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }
}
