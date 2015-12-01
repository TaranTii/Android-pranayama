package it.techies.pranayama.api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import it.techies.pranayama.api.ApiFields;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class LoginResponse
{
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("resource_id")
    @Expose
    private Integer resourceId;
    @SerializedName("History")
    @Expose
    private Integer History;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Integer getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(Integer resourceId)
    {
        this.resourceId = resourceId;
    }

    public Integer getHistory()
    {
        return History;
    }

    public void setHistory(Integer history)
    {
        History = history;
    }
}
