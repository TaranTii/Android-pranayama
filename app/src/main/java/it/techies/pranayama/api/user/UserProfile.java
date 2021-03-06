package it.techies.pranayama.api.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 09/12/2015.
 */
public class UserProfile {

    public static final String FIELD_FULL_NAME = "fullname";
    public static final String FIELD_DATE_OF_BIRTH = "dob";
    public static final String FIELD_ADDRESS = "address1";
    public static final String FIELD_CITY = "city";
    public static final String FIELD_STATE = "state";
    public static final String FIELD_PHONE = "phone";

    @SerializedName(FIELD_FULL_NAME)
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

    @SerializedName(FIELD_ADDRESS)
    @Expose
    private String address1;

    @SerializedName("address2")
    @Expose
    private String address2;

    @SerializedName(FIELD_CITY)
    @Expose
    private String city;

    @SerializedName(FIELD_STATE)
    @Expose
    private String state;

    @SerializedName("country_id")
    @Expose
    private String countryId;

    @SerializedName(FIELD_PHONE)
    @Expose
    private String phone;

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("timezone")
    @Expose
    private String timezone;

    @SerializedName(FIELD_DATE_OF_BIRTH)
    @Expose
    private String dob;

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

    public String getCountryId()
    {
        return countryId;
    }

    public void setCountryId(String countryId)
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
