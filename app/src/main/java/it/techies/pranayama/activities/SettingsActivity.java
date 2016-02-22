package it.techies.pranayama.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.utils.Utils;

public class SettingsActivity extends BaseActivity {

    @OnClick(R.id.item_profile)
    public void openProfile(View v)
    {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    @OnClick(R.id.item_history)
    public void openHistory(View v)
    {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    @OnClick(R.id.item_change_password)
    public void openChangePassword(View v)
    {
        startActivity(new Intent(this, ChangePasswordActivity.class));
    }

    @OnClick(R.id.item_general_settings)
    public void openGeneralSettings(View v)
    {

    }

    @OnClick(R.id.item_schedule_setup)
    public void openScheduleSetup(View v)
    {
        startActivity(new Intent(this, SetupActivity.class));
    }

    @OnClick(R.id.item_help)
    public void openHelp(View v)
    {

    }

    @OnClick(R.id.item_logout)
    public void doLogout(View v)
    {
        logoutUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
