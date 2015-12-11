package it.techies.pranayama.api.register;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 10/12/2015.
 */
public class RegisterRequest
{

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("fullname")
    @Expose
    private String fullname;

    @SerializedName("phone")
    @Expose
    private String phone;

    @SerializedName("signup_via")
    @Expose
    private String signupVia;

    @SerializedName("gender")
    @Expose
    private String gender;

    public RegisterRequest(String email, String password, String fullname, String gender,
                           String signupVia)
    {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.gender = gender;
        this.signupVia = signupVia;
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

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getSignupVia()
    {
        return signupVia;
    }

    public void setSignupVia(String signupVia)
    {
        this.signupVia = signupVia;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }
}
