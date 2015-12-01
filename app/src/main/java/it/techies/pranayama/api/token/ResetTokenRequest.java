package it.techies.pranayama.api.token;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class ResetTokenRequest
{
    @SerializedName("email")
    @Expose
    String email;

    @SerializedName("token")
    @Expose
    String token;

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }
}
