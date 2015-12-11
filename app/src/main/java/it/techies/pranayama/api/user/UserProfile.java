package it.techies.pranayama.api.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 09/12/2015.
 */
public class UserProfile
{
    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("fullname")
    @Expose
    private String fullname;

    @SerializedName("gender")
    @Expose
    private String gender;

    @SerializedName("created_at")
    @Expose
    private Integer createdAt;

    @SerializedName("updated_at")
    @Expose
    private Integer updatedAt;

    @SerializedName("type")
    @Expose
    private Integer type;

    @SerializedName("address1")
    @Expose
    private String address1;

    @SerializedName("address2")
    @Expose
    private String address2;

    @SerializedName("city")
    @Expose
    private String city;

    @SerializedName("state")
    @Expose
    private String state;

    @SerializedName("country_id")
    @Expose
    private Integer countryId;

    @SerializedName("phone")
    @Expose
    private String phone;

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("tw_id")
    @Expose
    private String twId;

    @SerializedName("fb_id")
    @Expose
    private String fbId;

    @SerializedName("timezone")
    @Expose
    private String timezone;

    @SerializedName("dob")
    @Expose
    private String dob;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public Integer getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Integer createdAt)
    {
        this.createdAt = createdAt;
    }

    public Integer getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(Integer updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getAddress1()
    {
        return address1;
    }

    public void setAddress1(String address1)
    {
        this.address1 = address1;
    }

    public String getAddress2()
    {
        return address2;
    }

    public void setAddress2(String address2)
    {
        this.address2 = address2;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public Integer getCountryId()
    {
        return countryId;
    }

    public void setCountryId(Integer countryId)
    {
        this.countryId = countryId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getTwId()
    {
        return twId;
    }

    public void setTwId(String twId)
    {
        this.twId = twId;
    }

    public String getFbId()
    {
        return fbId;
    }

    public void setFbId(String fbId)
    {
        this.fbId = fbId;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    public String getDob()
    {
        return dob;
    }

    public void setDob(String dob)
    {
        this.dob = dob;
    }
}
