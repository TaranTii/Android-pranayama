package it.techies.pranayama.modules.aasans.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import it.techies.pranayama.modules.aasans.Bhastrika;
import it.techies.pranayama.modules.aasans.KapalBhati;
import it.techies.pranayama.utils.FireRef;

/**
 * Created by jagdeep on 11/08/16.
 */
public class CurrentAasan implements Parcelable {

    private String mAasanKey;
    private int mCurrentSet;
    private int mNumberOfSets;
    private Class<?> mNextAasanClass;
    private Class<?> mCurrentAasanClass;

    /**
     * Default Constructor.
     *
     * @param aasanKey          Current aasan
     * @param currentAasanClass Current aasan class
     * @param nextAasanClass    Next aasan class
     * @param numberOfSets      Number of sets in current aasan
     * @param currentSet        current set of aasan
     */
    public CurrentAasan(
            @NonNull String aasanKey,
            @NonNull Class<?> currentAasanClass,
            @NonNull Class<?> nextAasanClass,
            @NonNull int numberOfSets,
            @NonNull int currentSet)
    {
        mAasanKey = aasanKey;
        mCurrentAasanClass = currentAasanClass;
        mNextAasanClass = nextAasanClass;
        mNumberOfSets = numberOfSets;
        mCurrentSet = currentSet;
    }

    /**
     * Constructor for first set of Aasan.
     *
     * @param aasanKey          Current aasan key
     * @param currentAasanClass Current aasan class
     * @param nextAasanClass    Next aasan class
     */
    public CurrentAasan(
            @NonNull String aasanKey,
            @NonNull Class<?> currentAasanClass,
            @NonNull Class<?> nextAasanClass)
    {
        mAasanKey = aasanKey;
        mCurrentAasanClass = currentAasanClass;
        mNextAasanClass = nextAasanClass;
        mCurrentSet = 1;
    }

    /**
     * Constructor for the first set of first Aasan.
     */
    public CurrentAasan()
    {
        mAasanKey = FireRef.REF_AASAN_BHASTRIKA;
        mCurrentSet = 1;
        mCurrentAasanClass = Bhastrika.class;
        mNextAasanClass = KapalBhati.class;
    }

    public String getAasanKey()
    {
        return mAasanKey;
    }

    public CurrentAasan setAasanKey(String aasanKey)
    {
        mAasanKey = aasanKey;
        return this;
    }

    public int getCurrentSet()
    {
        return mCurrentSet;
    }

    public CurrentAasan setCurrentSet(int currentSet)
    {
        mCurrentSet = currentSet;
        return this;
    }

    public int getNumberOfSets()
    {
        return mNumberOfSets;
    }

    public CurrentAasan setNumberOfSets(int numberOfSets)
    {
        mNumberOfSets = numberOfSets;
        return this;
    }

    public Class<?> getNextAasanClass()
    {
        return mNextAasanClass;
    }

    public CurrentAasan setNextAasanClass(Class<?> nextAasanClass)
    {
        mNextAasanClass = nextAasanClass;
        return this;
    }

    public Class<?> getCurrentAasanClass()
    {
        return mCurrentAasanClass;
    }

    public CurrentAasan setCurrentAasanClass(Class<?> currentAasanClass)
    {
        mCurrentAasanClass = currentAasanClass;
        return this;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.mAasanKey);
        dest.writeInt(this.mCurrentSet);
        dest.writeInt(this.mNumberOfSets);
        dest.writeSerializable(this.mNextAasanClass);
        dest.writeSerializable(this.mCurrentAasanClass);
    }

    protected CurrentAasan(Parcel in)
    {
        this.mAasanKey = in.readString();
        this.mCurrentSet = in.readInt();
        this.mNumberOfSets = in.readInt();
        this.mNextAasanClass = (Class<?>) in.readSerializable();
        this.mCurrentAasanClass = (Class<?>) in.readSerializable();
    }

    public static final Parcelable.Creator<CurrentAasan> CREATOR = new Parcelable.Creator<CurrentAasan>() {
        @Override
        public CurrentAasan createFromParcel(Parcel source)
        {
            return new CurrentAasan(source);
        }

        @Override
        public CurrentAasan[] newArray(int size)
        {
            return new CurrentAasan[size];
        }
    };

    public boolean isLastSet()
    {
        return mCurrentSet >= mNumberOfSets;
    }

    public void incrementCurrentSet()
    {
        mCurrentSet++;
    }
}
