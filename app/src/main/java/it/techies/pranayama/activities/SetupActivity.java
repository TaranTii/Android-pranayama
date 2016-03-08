package it.techies.pranayama.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.adapters.AasanListAdapter;
import it.techies.pranayama.api.AasanNames;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.api.timing.Timings;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.infrastructure.OnResetTokenSuccessCallBack;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class SetupActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @Bind(R.id.aasan_timing_lv)
    ListView mAasanTimingListView;

    @Bind(R.id.loading_ll)
    View mLoadingVIew;

    @Bind(R.id.reload_ll)
    View mReloadView;

    private AasanListAdapter adapter;

    private boolean mDidUserMadeChanges = false;
    private boolean mDidUserSavedChanges = false;

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

        mAasanTimingListView.setOnItemClickListener(this);

        getAasanTiming();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_update)
        {
            updateTimings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTimings()
    {
        mDidUserMadeChanges = false;
        List<AasanTime> aasanTimeList = adapter.getAasanList();
        setPranayamaTiming(aasanTimeList);
    }

    @Override
    public void onBackPressed()
    {
        if (mDidUserMadeChanges)
        {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.dialog_message_discard_changes)
                    .setPositiveButton(R.string.dialog_action_discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();

                            if (mDidUserSavedChanges)
                            {
                                setResult(RESULT_OK);
                            }
                            else
                            {
                                setResult(RESULT_CANCELED);
                            }
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

        }
        else
        {
            if (mDidUserSavedChanges)
            {
                setResult(RESULT_OK);
            }
            else
            {
                setResult(RESULT_CANCELED);
            }
            super.onBackPressed();
        }
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
        if(aasanTimeList.size() == 8)
        {
            aasanTimeList.remove(7);
        }

        showLoadingDialog("Updating...");
        Call<EmptyResponse> call = mApiClient.setPranayamaTiming(aasanTimeList);
        call.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    hideLoadingDialog();
                    mDidUserSavedChanges = true;
                    showToast("Updated...");
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
                    else
                    {
                        hideLoadingDialog();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                hideLoadingDialog();
                onRetrofitFailure(t);
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

                    Timber.d("Break time - %s", aasanTimes.get(0).getBreakTime());

                    aasanTimes.add(new AasanTime(aasanTimes.get(0).getBreakTime()));

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
                onRetrofitFailure(t);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        AasanTime aasanTime = adapter.getItem(position);
        picker(aasanTime, position);

        Timber.d("Position - %d", position);
    }

    public void picker(final AasanTime aasanTime, final int position)
    {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_aasan_setup, null);

        View mSetLinearLayout = dialogView.findViewById(R.id.set_ll);
        final NumberPicker sets = (NumberPicker) dialogView.findViewById(R.id.set_np);

        Timings timings;

        // hide sets in case of break
        if (position == 7)
        {
            mSetLinearLayout.setVisibility(View.GONE);

            timings = new Timings("00:00:00");
            try
            {
                timings.addSeconds(Long.valueOf(aasanTime.getBreakTime()));
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
                return;
            }
        }
        else
        {
            mSetLinearLayout.setVisibility(View.VISIBLE);

            sets.setMinValue(0);
            sets.setMaxValue(10);
            sets.setValue(aasanTime.getSet());

            timings = aasanTime.getTimings();
        }

        final NumberPicker minutes = (NumberPicker) dialogView.findViewById(R.id.minute_np);
        minutes.setMinValue(0);
        minutes.setMaxValue(15);
        minutes.setValue((int) timings.getMinutes());

        Timber.d("Minutes %d, s %d", (int) timings.getMinutes(), (int) timings.getSeconds());

        final NumberPicker seconds = (NumberPicker) dialogView.findViewById(R.id.second_np);
        seconds.setMinValue(0);
        seconds.setMaxValue(59);
        seconds.setValue((int) timings.getSeconds());


        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mDidUserMadeChanges = true;

                        int selectedMinutes = minutes.getValue();
                        int selectedSeconds = seconds.getValue();
                        int selectedSets = sets.getValue();

                        String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", 0, selectedMinutes, selectedSeconds);

                        if(position == 7)
                        {
                            Timings breakTimings = new Timings(time);
                            adapter.setBreakTime(breakTimings.getTotalTimeInSeconds());
                        }
                        else
                        {
                            aasanTime.setSet(selectedSets);
                            aasanTime.setTime(time);
                            adapter.notifyDataSetChanged();
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                })
                .show();
    }
}
