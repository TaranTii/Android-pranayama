package it.techies.pranayama.api.timing;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class AasanTime implements Parcelable {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("time")
    @Expose
    private String time;

    @SerializedName("set")
    @Expose
    private Integer set;

    @SerializedName("break_time")
    @Expose
    private String breakTime;

    public AasanTime(String name, String time, Integer set)
    {
        this.name = name;
        this.time = time;
        this.set = set;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTime()
    {
        return time;
    }

    public Timings getTimings()
    {
        return new Timings(time);
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public Integer getSet()
    {
        return set;
    }

    public void setSet(Integer set)
    {
        this.set = set;
    }

    public String getBreakTime()
    {
        return breakTime;
    }

    public void setBreakTime(String breakTime)
    {
        this.breakTime = breakTime;
    }

    // ----------------------------------------- Parcelable ------------------------------------ //

    protected AasanTime(Parcel in)
    {
        name = in.readString();
        time = in.readString();
        set = in.readByte() == 0x00 ? null : in.readInt();
        breakTime = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(time);
        if (set == null)
        {
            dest.writeByte((byte) (0x00));
        }
        else
        {
            dest.writeByte((byte) (0x01));
            dest.writeInt(set);
        }
        dest.writeString(breakTime);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AasanTime> CREATOR = new Parcelable.Creator<AasanTime>() {
        @Override
        public AasanTime createFromParcel(Parcel in)
        {
            return new AasanTime(in);
        }

        @Override
        public AasanTime[] newArray(int size)
        {
            return new AasanTime[size];
        }
    };

    // ----------------------------------------------------------------------------------------- //
}