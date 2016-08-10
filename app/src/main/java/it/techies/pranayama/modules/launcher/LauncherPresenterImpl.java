package it.techies.pranayama.modules.launcher;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.techies.pranayama.utils.FireRef;

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
            DatabaseReference setupPref = FirebaseDatabase.getInstance()
                    .getReference(FireRef.REF_USERS)
                    .child(uid)
                    .child(FireRef.REF_USER_SETUP);

            setupPref.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            Boolean isSetupCompleted = dataSnapshot.getValue(Boolean.class);

                            if (dataSnapshot.exists() && isSetupCompleted)
                            {
                                if (mView != null)
                                {
                                    mView.showSetupView(false);
                                }
                            }
                            else
                            {
                                if (mView != null)
                                {
                                    mView.showSetupView(true);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                            if (mView != null)
                            {
                                mView.showToastMessage(databaseError.getMessage());
                            }
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
