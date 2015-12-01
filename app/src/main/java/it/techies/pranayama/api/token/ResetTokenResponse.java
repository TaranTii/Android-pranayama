package it.techies.pranayama.api.token;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import it.techies.pranayama.api.ApiFields;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class ResetTokenResponse
{
    @SerializedName(ApiFields.FIELD_TOKEN)
    @Expose
    String token;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }
}
