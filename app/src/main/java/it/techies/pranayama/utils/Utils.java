package it.techies.pranayama.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import it.techies.pranayama.activities.LoginActivity;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.errors.ErrorObject;
import it.techies.pranayama.api.token.ResetTokenCallBack;
import it.techies.pranayama.api.token.ResetTokenRequest;
import it.techies.pranayama.api.token.ResetTokenResponse;
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

    /**
     * Shows the loading dialog
     */
    public static void showLoadingDialog(ProgressDialog progressDialog, String message)
    {
        if (progressDialog != null && !progressDialog.isShowing())
        {
            progressDialog.show();
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }

    /**
     * Hides the loading dialog
     */
    public static void hideLoadingDialog(ProgressDialog progressDialog)
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
    }

    /**
     * Logout the user and start Launcher activity in new task stack.
     *
     * @param context Context from where this function is called.
     */
    public static void logoutUser(Context context)
    {
        SessionStorage settings = new SessionStorage(context);
        settings.clearUserData();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Sends API request to Reset Token
     */
    public static void resetToken(final Context context, ApiClient.ApiInterface apiClient,
                                  String email, String token, final ResetTokenCallBack callBack)
    {
        ResetTokenRequest resetTokenRequest = new ResetTokenRequest();
        resetTokenRequest.setEmail(email);
        resetTokenRequest.setToken(token);

        final Call<ResetTokenResponse> call = apiClient.resetToken(resetTokenRequest);

        call.enqueue(new Callback<ResetTokenResponse>()
        {
            @Override
            public void onResponse(Response<ResetTokenResponse> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    ResetTokenResponse resetTokenResponse = response.body();
                    // get the updated token
                    callBack.onSuccess(resetTokenResponse.getToken());
                }
                else
                {
                    Utils.logoutUser(context);
                    Toast.makeText(context, "You're logged out because of security reasons.", Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.e(t, "resetTokenRequest");
                Utils.logoutUser(context);
                Toast.makeText(context, "You're logged out because of security reasons.", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}