package it.techies.pranayama.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

import it.techies.pranayama.utils.FireRef;

/**
 * Schedule model of aasans and break for firebase database.
 * <p/>
 * Created by jagdeep on 29/07/16.
 */
public class FirebaseSchedule {

    @Exclude
    public static final String TYPE_AASAN = "aasan";

    @Exclude
    public static final String TYPE_BREAK = "break";

    public String key;
    public String uid;
    public String type;
    public String name;
    public Integer order;
    public Integer duration;
    public Integer numberOfSets;

    /**
     * Constructor for aasan schedule.
     *
     * @param key          Aasan key
     * @param uid          User id
     * @param name         Aasan name
     * @param order        Aasan order
     * @param duration     Duration of aasan in seconds
     * @param numberOfSets Number of sets in aasan
     */
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

    /**
     * Constructor for break schedule.
     *
     * @param uid      User id
     * @param duration Duration of break in seconds
     */
    public FirebaseSchedule(String uid, Integer duration)
    {
        this.key = FireRef.REF_AASAN_BREAK;
        this.uid = uid;
        this.type = TYPE_BREAK;
        this.name = "Break";
        this.duration = duration;
        this.order = 99;
    }

    /**
     * Constructor for firebase.
     */
    public FirebaseSchedule()
    {
    }

    /**
     * Save current instance of schedule to firebase.
     */
    @Exclude
    public void save()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(FireRef.REF_USER_PREFS);

        ref.child(this.uid)
                .child(this.key)
                .setValue(this);
    }

    @Exclude
    public int getMinutes()
    {
        return this.duration / 60;
    }

    @Exclude
    public int getSeconds()
    {
        return this.duration % 60;
    }

    @Exclude
    public String getTimeString()
    {
        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                getMinutes(),
                getSeconds()
        );
    }
}
