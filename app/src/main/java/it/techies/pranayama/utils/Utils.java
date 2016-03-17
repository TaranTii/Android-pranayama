package it.techies.pranayama.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.errors.ErrorObject;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class Utils {

    /**
     * Function to convert bitmap to string
     *
     * @param pic
     * @return String
     */
    public static String bitmapToString(Bitmap pic)
    {
        if (pic != null)
        {
            //convert bitmap to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            pic.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // byte array encode to base64
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        else
        {
            return "";
        }
    }

    /**
     * Parses the error response object
     *
     * @param errorBody
     * @return
     * @throws JsonSyntaxException
     */
    public static ErrorObject getErrorObject(
            ResponseBody errorBody) throws JsonSyntaxException, IOException
    {
        Gson gson = new Gson();
        return gson.fromJson(errorBody.string(), ErrorObject.class);
    }

    /**
     * Parses the error response array
     *
     * @param errorBody
     * @return
     * @throws JsonSyntaxException
     */
    public static List<ErrorArray> getErrorList(
            ResponseBody errorBody) throws JsonSyntaxException, IOException
    {
        Gson gson = new Gson();

        Type listType = new TypeToken<List<ErrorArray>>() {
        }.getType();

        return gson.fromJson(errorBody.string(), listType);
    }

    public static boolean isDateOfBirthValid(@NonNull String dateOfBirth)
    {
        final String DATE_OF_BIRTH_REGEX = "\\d{4}?-\\d{2}?-\\d{2}";

        // dob should match the dob format
        if (!dateOfBirth.matches(DATE_OF_BIRTH_REGEX))
        {
            return false;
        }

        String[] dob = dateOfBirth.split("-", 3);

        // sanity check for array length
        if (dob.length != 3)
        {
            return false;
        }

        int year = Integer.valueOf(dob[0]);
        int month = Integer.valueOf(dob[1]);
        int day = Integer.valueOf(dob[2]);

        Calendar cal = Calendar.getInstance();

        // year should be greater than 0 and less than current year
        if (year < 1 || year >= cal.get(Calendar.YEAR))
        {
            return false;
        }

        // month should be between 1 - 12
        if (month < 1 || month > 12)
        {
            return false;
        }

        // day should be between 1 - 31
        if (day < 1 || day > 31)
        {
            return false;
        }

        return true;
    }

}
