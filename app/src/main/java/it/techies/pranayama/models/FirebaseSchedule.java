package it.techies.pranayama.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.annotations.Expose;

/**
 * Created by jagdeep on 29/07/16.
 */
public class FirebaseSchedule {

    @Exclude
    public static final String TYPE_AASAN = "aasan";

    @Expose
    public static final String TYPE_BREAK = "break";

    public String key;
    public String uid;
    public String type;
    public String name;
    public Integer order;
    public Integer duration;
    public Integer numberOfSets;

    public FirebaseSchedule(String key, String uid, String name, Integer order, Integer duration, Integer numberOfSets)
    {
        this.key = key;
        this.uid = uid;
        this.type = TYPE_AASAN;
        this.name = name;
        this.duration = duration;
        this.order = order;
        this.numberOfSets = numberOfSets;
    }

    public FirebaseSchedule(String uid, Integer duration)
    {
        this.uid = uid;
        this.type = TYPE_BREAK;
        this.duration = duration;
        this.order = 99;
    }

    public FirebaseSchedule()
    {
    }

    public void save()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("prefs");

        ref.child(this.uid)
                .child(this.key)
                .setValue(this);
    }
}
