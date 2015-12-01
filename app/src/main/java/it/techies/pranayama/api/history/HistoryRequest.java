package it.techies.pranayama.api.history;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class HistoryRequest
{
    @SerializedName("date")
    @Expose
    String date;

    public HistoryRequest(String date)
    {
        this.date = date;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }
}
