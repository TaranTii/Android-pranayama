package it.techies.pranayama.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jagdeep on 28/01/16.
 */
public class DailyRoutine implements Parcelable {

    @SerializedName("time_zone")
    @Expose
    private String timeZone;

    @SerializedName("Anulom Vilom")
    @Expose
    private String anulomVilom;

    @SerializedName("time")
    @Expose
    private String time;

    @SerializedName("Bharmari")
    @Expose
    private String bharmari;

    @SerializedName("Udgeeth")
    @Expose
    private String udgeeth;

    @SerializedName("Bahi")
    @Expose
    private String bahi;

    @SerializedName("Agnisar Kriya")
    @Expose
    private String agnisarKriya;

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("gmt")
    @Expose
    private String gmt;

    @SerializedName("Bhastrika")
    @Expose
    private String bhastrika;

    @SerializedName("Kapalbhati")
    @Expose
    private String kapalbhati;

    public String getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }

    public String getAnulomVilom()
    {
        return anulomVilom;
    }

    public void setAnulomVilom(String anulomVilom)
    {
        this.anulomVilom = anulomVilom;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getBharmari()
    {
        return bharmari;
    }

    public void setBharmari(String bharmari)
    {
        this.bharmari = bharmari;
    }

    public String getUdgeeth()
    {
        return udgeeth;
    }

    public void setUdgeeth(String udgeeth)
    {
        this.udgeeth = udgeeth;
    }

    public String getBahi()
    {
        return bahi;
    }

    public void setBahi(String bahi)
    {
        this.bahi = bahi;
    }

    public String getAgnisarKriya()
    {
        return agnisarKriya;
    }

    public void setAgnisarKriya(String agnisarKriya)
    {
        this.agnisarKriya = agnisarKriya;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getGmt()
    {
        return gmt;
    }

    public void setGmt(String gmt)
    {
        this.gmt = gmt;
    }

    public String getBhastrika()
    {
        return bhastrika;
    }

    public void setBhastrika(String bhastrika)
    {
        this.bhastrika = bhastrika;
    }

    public String getKapalbhati()
    {
        return kapalbhati;
    }

    public void setKapalbhati(String kapalbhati)
    {
        this.kapalbhati = kapalbhati;
    }

    protected DailyRoutine(Parcel in)
    {
        timeZone = in.readString();
        anulomVilom = in.readString();
        time = in.readString();
        bharmari = in.readString();
        udgeeth = in.readString();
        bahi = in.readString();
        agnisarKriya = in.readString();
        date = in.readString();
        gmt = in.readString();
        bhastrika = in.readString();
        kapalbhati = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(timeZone);
        dest.writeString(anulomVilom);
        dest.writeString(time);
        dest.writeString(bharmari);
        dest.writeString(udgeeth);
        dest.writeString(bahi);
        dest.writeString(agnisarKriya);
        dest.writeString(date);
        dest.writeString(gmt);
        dest.writeString(bhastrika);
        dest.writeString(kapalbhati);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<DailyRoutine> CREATOR = new Parcelable.Creator<DailyRoutine>() {
        @Override
        public DailyRoutine createFromParcel(Parcel in)
        {
            return new DailyRoutine(in);
        }

        @Override
        public DailyRoutine[] newArray(int size)
        {
            return new DailyRoutine[size];
        }
    };
}