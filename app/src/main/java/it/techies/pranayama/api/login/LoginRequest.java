package it.techies.pranayama.api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import it.techies.pranayama.api.ApiFields;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class LoginRequest
{
    @SerializedName(ApiFields.FIELD_EMAIL)
    @Expose
    String email;

    @SerializedName(ApiFields.FIELD_PASSWORD)
    @Expose
    String password;

    @SerializedName("login_via")
    @Expose
    String loginVia;

    public LoginRequest(String email, String password, String loginVia)
    {
        this.email = email;
        this.password = password;
        this.loginVia = loginVia;
    }

    public String getLoginVia()
    {
        return loginVia;
    }

    public void setLoginVia(String loginVia)
    {
        this.loginVia = loginVia;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "LoginRequest{" +
                "password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
