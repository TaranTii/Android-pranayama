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
import java.util.Locale;

import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.errors.ErrorObject;

/**
 * Created by jagdeep on 30/11/2015.
 */
public class Utils {

    /**
     * Converts bitmap to Base64 string.
     *
     * @param bitmap
     * @return String
     */
    public static String bitmapToString(Bitmap bitmap)
    {
        if (bitmap != null)
        {
            //convert bitmap to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // byte array encode to base64
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        else
        {
            return "";
        }
    }

    public static String getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();
        return getDateFromCalender(calendar);
    }

    public static String getDateFromCalender(Calendar calendar)
    {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        return String.format(
                Locale.getDefault(),
                "%d-%02d-%02d",
                year,
                month,
                day);
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
