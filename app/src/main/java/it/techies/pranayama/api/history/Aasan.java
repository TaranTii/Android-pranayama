package it.techies.pranayama.api.history;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class Aasan
{
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("gmt")
    @Expose
    private String gmt;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("time_zone")
    @Expose
    private String timeZone;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("is_completed")
    @Expose
    private Integer isCompleted;

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

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getIsCompleted()
    {
        return isCompleted;
    }

    public void setIsCompleted(Integer isCompleted)
    {
        this.isCompleted = isCompleted;
    }
}
