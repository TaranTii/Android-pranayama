package it.techies.pranayama.api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import it.techies.pranayama.api.ApiFields;

/**
 * Created by jdtechies on 09/12/2015.
 */
public class ChangePasswordRequest
{
    @SerializedName(ApiFields.FIELD_CURRENT_PASSWORD)
    @Expose
    String currentPassword;

    @SerializedName(ApiFields.FIELD_NEW_PASSWORD)
    @Expose
    String newPassword;

    public ChangePasswordRequest(String currentPassword, String newPassword)
    {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword()
    {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword)
    {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }
}
