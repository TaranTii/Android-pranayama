package it.techies.pranayama.api.timing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class AasanTime
{
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
}
