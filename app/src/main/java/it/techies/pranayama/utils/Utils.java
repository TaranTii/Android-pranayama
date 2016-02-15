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

    /**
     * Shows the loading dialog
     */
    public static void showLoadingDialog(ProgressDialog progressDialog, String message)
    {
        if (progressDialog != null && !progressDialog.isShowing())
        {
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(message);
            progressDialog.show();
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
        ApplicationSettings settings = new ApplicationSettings(context);
        settings.clearUserData();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        context.stopService(new Intent(context, PrayanamaService.class));
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
                    Utils.showToast(context, "You're logged out because of security reasons.");
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

    /**
     * Displays a toast with given message.
     *
     * @param message Message to show in toast.
     */
    public static void showToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle retrofit failure error.
     *
     * @param context Context
     * @param t Exception to handle
     */
    public static void handleRetrofitFailure(Context context, Throwable t)
    {
        if (t == null)
        {
            Utils.showToast(context, "Unknown error");
            return;
        }

        Timber.d(t.toString());

        if (t instanceof SocketTimeoutException)
        {
            Utils.showToast(context, "Socket Timeout Exception");
        }
        else if (t instanceof UnknownHostException)
        {
            Utils.showToast(context, "Unknown Host Exception");
        }
        else
        {
            Utils.showToast(context, t.getMessage());
            // Utils.showToast(context, "Please check your internet connection");
        }
    }

    /**
     * Show error dialog to User.
     *
     * @param message Message to display.
     */
    public static void showErrorDialog(Context context, String message)
    {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message).setTitle("Error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
