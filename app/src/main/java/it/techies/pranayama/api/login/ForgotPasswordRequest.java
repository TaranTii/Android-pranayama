package it.techies.pranayama.api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import it.techies.pranayama.api.ApiFields;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class ForgotPasswordRequest
{
    @SerializedName(ApiFields.FIELD_EMAIL)
    @Expose
    String email;

    public ForgotPasswordRequest(String email)
    {
        this.email = email;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Override
    public String toString()
    {
        return "ForgotPasswordRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
