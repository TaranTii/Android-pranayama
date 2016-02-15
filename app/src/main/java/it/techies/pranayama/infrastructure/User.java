package it.techies.pranayama.infrastructure;

import it.techies.pranayama.utils.ApplicationSettings;

/**
 * Created by jagdeep on 28/01/16.
 */
public class User {

    private String email;

    private String fullName;

    private String phoneNumber;

    private String profilePhoto;

    private Integer userId;

    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePhoto()
    {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto)
    {
        this.profilePhoto = profilePhoto;
    }

    public void update(ApplicationSettings settings)
    {
        settings.setPhoneNumber(this.phoneNumber);
        settings.setFullName(this.fullName);
        settings.setProfilePhotoUrl(this.profilePhoto);
        settings.setEmailPreference(this.email);
        settings.setResourceIdPreference(this.userId);
    }

    public static User getUser(ApplicationSettings settings)
    {
        User user = new User();
        user.setEmail(settings.getEmailPreference());
        user.setFullName(settings.getFullName());
        user.setPhoneNumber(settings.getPhoneNumber());
        user.setProfilePhoto(settings.getProfilePhotoUrl());
        user.setUserId(settings.getResourceIdPreference());
        return user;
    }
}
