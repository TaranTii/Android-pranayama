package it.techies.pranayama.api;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class SuccessResponse
{
    String field, message;

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        return "SuccessResponse{" +
                "field='" + field + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
