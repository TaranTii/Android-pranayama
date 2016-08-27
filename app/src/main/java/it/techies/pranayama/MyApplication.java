package it.techies.pranayama;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();

        // enable Disk Persistence
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(false);
        }
    }

}
