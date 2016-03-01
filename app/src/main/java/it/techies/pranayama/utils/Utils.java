package it.techies.pranayama.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import it.techies.pranayama.activities.LoginActivity;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.errors.ErrorObject;
import it.techies.pranayama.api.token.ResetTokenCallBack;
import it.techies.pranayama.api.token.ResetTokenRequest;
import it.techies.pranayama.api.token.ResetTokenResponse;
import it.techies.pranayama.services.PrayanamaService;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class Utils
{
    /**
     * Function to convert bitmap to string
     *
     * @param pic
     *
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
     *
     * @return
     *
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
     *
     * @return
     *
     * @throws JsonSyntaxException
     */
    public static List<ErrorArray> getErrorList(
            ResponseBody errorBody) throws JsonSyntaxException, IOException
    {
        Gson gson = new Gson();

        Type listType = new TypeToken<List<ErrorArray>>()
        {
        }.getType();

        return gson.fromJson(errorBody.string(), listType);
    }

}
