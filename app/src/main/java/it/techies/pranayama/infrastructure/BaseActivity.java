package it.techies.pranayama.infrastructure;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import it.techies.pranayama.MyApplication;
import it.techies.pranayama.R;
import it.techies.pranayama.activities.LoginActivity;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.token.ResetTokenRequest;
import it.techies.pranayama.api.token.ResetTokenResponse;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by jagdeep on 28/01/16.
 */
public abstract class BaseActivity extends AppCompatActivity implements Auth.TokenChangeListener {

    protected MyApplication mApplication;
    protected Bus mBus;
    protected ApiClient.ApiInterface mApiClient;
    protected Auth mAuth;
    protected ProgressDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mApplication = (MyApplication) getApplication();
        mAuth = mApplication.getAuth();
        mBus = mApplication.getBus();
        mBus.register(this);
        mApiClient = ApiClient.getApiClient(mAuth.getUser().getEmail(), mAuth.getToken());
        mAuth.setTokenChangeListener(this);
    }

    @Override
    public void startActivity(Intent intent)
    {
        super.startActivity(intent);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mBus.unregister(this);
    }

    /**
     * Shows the loading dialog
     */
    protected void showLoadingDialog(String message)
    {
        if (mDialog == null)
        {
            mDialog = new ProgressDialog(this);
        }

        if (!mDialog.isShowing())
        {
            mDialog.show();
            mDialog.setMessage(message);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
        }
    }

    /**
     * Hides the loading dialog
     */
    protected void hideLoadingDialog()
    {
        if (mDialog != null && mDialog.isShowing())
        {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle retrofit failure error.
     *
     * @param t Exception to handle
     */
    protected void onRetrofitFailure(Throwable t)
    {
        if (t == null)
        {
            showToast("Unknown error");
            return;
        }

        if (t instanceof SocketTimeoutException)
        {
            showToast("Socket Timeout Exception");
        }
        else if (t instanceof UnknownHostException)
        {
            showToast("Unknown Host Exception");
        }
        else
        {
            if (t.getMessage() != null)
            {
                showToast(t.getMessage());
            }
            else
            {
                showToast("Please check your internet connection");
            }
        }
    }

    /**
     * Sends API request to Reset Token
     */
    protected void resetToken(final OnResetTokenSuccessCallBack callBack)
    {
        ResetTokenRequest resetTokenRequest = new ResetTokenRequest();
        resetTokenRequest.setEmail(mAuth.getUser().getEmail());
        resetTokenRequest.setToken(mAuth.getToken());

        Call<ResetTokenResponse> call = mApiClient.resetToken(resetTokenRequest);
        call.enqueue(new Callback<ResetTokenResponse>() {
            @Override
            public void onResponse(Response<ResetTokenResponse> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    if (response.body() != null)
                    {
                        Timber.d("resetToken : onSuccess()");
                        onTokenChanged(response.body().getToken());
                        callBack.onSuccess(response.body().getToken());
                    }
                    else
                    {
                        logoutUser();
                        showToast("You're logged out because of security reasons");
                    }
                }
                else
                {
                    logoutUser();
                    showToast("You're logged out because of security reasons");
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                logoutUser();
                Timber.e(t, "resetToken()");
                showToast("You're logged out because of security reasons");
            }
        });
    }

    /**
     * Logout the user and starts the Launcher activity.
     */
    protected void logoutUser()
    {
        Call<EmptyResponse> call = mApiClient.doSignOut(mAuth.getUser().getUserId());
        call.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    Timber.d("logoutUser() - isSuccess");
                }
                else
                {
                    Timber.d("logoutUser() - failed");
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                Timber.d("logoutUser() - onFailure");
            }
        });

        mAuth.logout(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected void showAlert(String message)
    {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onTokenChanged(@Nullable String token)
    {
        Timber.d("onTokenChanged %s", token);

        mApplication.setAuth(mAuth);

        if (token == null || mAuth.getUser() == null || mAuth.getUser().getEmail() == null)
        {
            mApiClient = ApiClient.getApiClient(null, null);
        }
        else
        {
            mApiClient = ApiClient.getApiClient(mAuth.getUser().getEmail(), token);
        }
    }
}

