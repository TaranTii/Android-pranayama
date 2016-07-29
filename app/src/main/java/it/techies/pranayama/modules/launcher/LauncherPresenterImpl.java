package it.techies.pranayama.modules.launcher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.techies.pranayama.modules.launcher.models.UserPrefsMeta;

/**
 * Created by jagdeep on 29/07/16.
 */
public class LauncherPresenterImpl implements LauncherPresenter {

    private LauncherView mView;
    private FirebaseUser mUser;

    public LauncherPresenterImpl(LauncherView view, FirebaseUser user)
    {
        mView = view;
        mUser = user;

        // init functions
        readUserPrefs(mUser.getUid());
    }

    private void readUserPrefs(String uid)
    {
        if (mView != null)
        {
            DatabaseReference userPrefs = FirebaseDatabase.getInstance()
                    .getReference("prefs-meta")
                    .child(uid);

            userPrefs.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            UserPrefsMeta meta = dataSnapshot.getValue(UserPrefsMeta.class);

                            if (dataSnapshot.exists())
                            {
                                if (meta.userCompletedSetup)
                                {
                                    mView.showSetupView(false);
                                }
                                else
                                {
                                    mView.showSetupView(true);
                                }
                            }
                            else
                            {
                                mView.showSetupView(true);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                            mView.showToastMessage(databaseError.getMessage());
                        }
                    });
        }
    }


    @Override
    public void onDestroy()
    {
        mView = null;
    }
}
