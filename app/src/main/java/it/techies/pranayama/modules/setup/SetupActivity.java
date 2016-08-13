package it.techies.pranayama.modules.setup;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.models.FirebaseSchedule;
import it.techies.pranayama.utils.DividerItemDecoration;
import it.techies.pranayama.utils.FireRef;

public class SetupActivity extends BaseActivity implements SetupView {

    private static final String TAG = "SetupActivity";

    @Bind(R.id.aasan_timing_rv)
    RecyclerView mAasanTimingRecyclerView;

    @Bind(R.id.loading_ll)
    View mLoadingVIew;

    private RecyclerView mRecycler;

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

        // mark initial setup completed for this user
        markInitialSetupCompleted();
    }

    private void markInitialSetupCompleted()
    {
        final FirebaseUser user = getUser();
        if (user != null)
        {
            DatabaseReference setupRef = FirebaseDatabase.getInstance()
                    .getReference(FireRef.REF_USERS)
                    .child(user.getUid())
                    .child(FireRef.REF_USER_SETUP);

            setupRef.setValue(true);
        }
    }

    private void showLoading(boolean state)
    {
        mAasanTimingRecyclerView.setVisibility(state ? View.GONE : View.VISIBLE);
        mLoadingVIew.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private void getAasanTiming()
    {
        showLoading(true);

        // Set up Layout Manager, reverse layout
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(manager);
        mRecycler.addItemDecoration(new DividerItemDecoration(this));

        final String uid = getUid();

        // Set up FirebaseRecyclerAdapter with the Query
        Query query = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_USER_PREFS)
                .child(uid)
                .orderByChild("order");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                showLoading(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                showLoading(false);
                showToast(databaseError.getMessage());
            }
        });

        FirebaseRecyclerAdapter<FirebaseSchedule, SetupViewHolder> adapter = new FirebaseRecyclerAdapter<FirebaseSchedule, SetupViewHolder>(
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

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View itemView)
                    {
                        Log.d(TAG, "onClick() called with: " + "itemView = [" + itemView + "]");
                        showSchedulePicker(model, position);
                    }
                });
            }
        };

        mRecycler.setAdapter(adapter);
    }

    public void showSchedulePicker(final FirebaseSchedule model, final int position)
    {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_aasan_setup, null);

        View mSetLinearLayout = dialogView.findViewById(R.id.set_ll);
        final NumberPicker sets = (NumberPicker) dialogView.findViewById(R.id.set_np);

        // hide mSets in case of break
        if (position == 7)
        {
            mSetLinearLayout.setVisibility(View.GONE);
        }
        else
        {
            mSetLinearLayout.setVisibility(View.VISIBLE);

            sets.setMinValue(1);
            sets.setMaxValue(10);
            sets.setValue(model.numberOfSets);
        }

        final NumberPicker minutes = (NumberPicker) dialogView.findViewById(R.id.minute_np);
        minutes.setMinValue(0);
        minutes.setMaxValue(15);
        minutes.setValue(model.getMinutes());

        final NumberPicker seconds = (NumberPicker) dialogView.findViewById(R.id.second_np);
        seconds.setMinValue(0);
        seconds.setMaxValue(59);
        seconds.setValue(model.getSeconds());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_action_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        int selectedMinutes = minutes.getValue();
                        int selectedSeconds = seconds.getValue();
                        int selectedSets = sets.getValue();

                        if (selectedMinutes == 0 && selectedSeconds < 15)
                        {
                            showMinimumTimeDialog();
                            return;
                        }

                        if (position == 7)
                        {
                            // update break time
                            model.duration = (selectedMinutes * 60) + selectedSeconds;
                            model.save();
                        }
                        else
                        {
                            // update aasan time
                            model.duration = (selectedMinutes * 60) + selectedSeconds;
                            model.numberOfSets = selectedSets;
                            model.save();
                        }

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showMinimumTimeDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(R.string.less_than_14_error_message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }
}
