package it.techies.pranayama.api.errors;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class ErrorArray
{
    @SerializedName("field")
    @Expose
    private String field;

    @SerializedName("message")
    @Expose
    private String message;

    /**
     * @return The field
     */
    public String getField()
    {
        return field;
    }

    /**
     * @param field The field
     */
    public void setField(String field)
    {
        this.field = field;
    }

    /**
     * @return The message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message The message
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        return "ErrorArray{" +
                "message='" + message + '\'' +
                ", field='" + field + '\'' +
                '}';
    }
}
