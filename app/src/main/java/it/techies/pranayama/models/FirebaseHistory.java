package it.techies.pranayama.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Map;

import it.techies.pranayama.utils.FireRef;

/**
 * To keep the history of each completed set of aasan in firebase database.
 * <p/>
 * Created by jagdeep on 10/08/16.
 */
public class FirebaseHistory {

    public String aasanKey;
    public int duration;
    public Map<String, String> timestamp;

    public FirebaseHistory(String aasanKey, int duration)
    {
        this.aasanKey = aasanKey;
        this.duration = duration;
    }

    public FirebaseHistory()
    {
    }

    @Exclude
    public void save(@NonNull String uid, @NonNull String date)
    {
        this.timestamp = ServerValue.TIMESTAMP;

        DatabaseReference aasansRef = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_HISTORY)
                .child(uid)
                .child(date);

        aasansRef.push().setValue(this);
    }

}
