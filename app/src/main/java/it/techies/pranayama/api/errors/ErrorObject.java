package it.techies.pranayama.api.errors;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class ErrorObject
{
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("code")
    @Expose
    private Integer code;

    @SerializedName("status")
    @Expose
    private Integer status;

    @SerializedName("type")
    @Expose
    private String type;

    /**
     * @return The name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name)
    {
        this.name = name;
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

    /**
     * @return The code
     */
    public Integer getCode()
    {
        return code;
    }

    /**
     * @param code The code
     */
    public void setCode(Integer code)
    {
        this.code = code;
    }

    /**
     * @return The status
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * @param status The status
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * @return The type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "ErrorObject{" +
                "type='" + type + '\'' +
                ", status=" + status +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
