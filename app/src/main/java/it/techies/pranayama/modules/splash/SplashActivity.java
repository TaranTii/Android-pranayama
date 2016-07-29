package it.techies.pranayama.modules.splash;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.models.FirebaseAasan;
import it.techies.pranayama.models.FirebaseSchedule;
import it.techies.pranayama.modules.launcher.LauncherActivity;
import timber.log.Timber;

public class SplashActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    DatabaseReference mAasanRef;
    DatabaseReference mUsersRef;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        // checks if user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null)
        {
            navigateToLauncherActivity();
        }

        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setMessage("Please wait...");

        // firebase database refs
        mAasanRef = FirebaseDatabase.getInstance().getReference("aasans");
        mUsersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void navigateToLauncherActivity()
    {
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            if (resultCode == RESULT_OK)
            {
                mDialog.show();
                updateUserData();
            }
            else
            {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
                Toast.makeText(SplashActivity.this, "Unable to sign in, please try again...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUserData()
    {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            DatabaseReference userRef = mUsersRef.child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (!dataSnapshot.exists())
                    {
                        Timber.d("updateUserData: new_user");

                        writeUserToDatabase(user);
                        writeUserPrefs(user);

                        mDialog.dismiss();
                        navigateToLauncherActivity();
                    }
                    else
                    {
                        Timber.d("updateUserData: existing_user");

                        mDialog.dismiss();
                        navigateToLauncherActivity();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    mDialog.dismiss();
                    Timber.e(databaseError.toException(), "updateUserData: onCancelled");
                }
            });
        }
    }

    private void writeUserToDatabase(FirebaseUser user)
    {
        if (user != null)
        {
            mUsersRef.child(user.getUid()).setValue(user);
        }
    }

    private void writeUserPrefs(final FirebaseUser user)
    {
        final String uid = user.getUid();

        mAasanRef.orderByChild("order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    FirebaseAasan aasan = data.getValue(FirebaseAasan.class);

                    String aasanKey = data.getKey();
                    String aasanName = aasan.name;
                    Integer aasanOrder = aasan.order;

                    // save aasan schedule under users uid
                    FirebaseSchedule pref = new FirebaseSchedule(
                            aasanKey,
                            uid,
                            aasanName,
                            aasanOrder,
                            // duration
                            60,
                            // number of sets
                            1);

                    pref.save();
                }

                // save default break duration
                FirebaseSchedule breakSchedule = new FirebaseSchedule(uid, 60);
                breakSchedule.save();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Timber.d("onCancelled: %s", databaseError.getDetails());
            }
        });
    }

    @OnClick(R.id.get_started_btn)
    public void getStartedButtonClick(View v)
    {
        startActivityForResult(
                // Get an instance of AuthUI based on the default app
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.EMAIL_PROVIDER,
                                AuthUI.GOOGLE_PROVIDER,
                                AuthUI.FACEBOOK_PROVIDER)
                        .setLogo(R.drawable.ic_logo)
                        .build(),
                RC_SIGN_IN);
    }

}
