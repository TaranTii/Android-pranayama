package it.techies.pranayama.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.List;

import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.ApiFields;
import it.techies.pranayama.api.SuccessResponse;
import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.login.ForgotPasswordRequest;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * A forgot password screen that offers to reset password via email.
 */
public class ForgotPasswordActivity extends BaseActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Call<SuccessResponse> mAuthTask = null;

    // UI references.
    private EditText mEmailView;

    Callback<SuccessResponse> mForgotPasswordCallback = new Callback<SuccessResponse>() {
        @Override
        public void onResponse(Response<SuccessResponse> response, Retrofit retrofit)
        {
            Timber.d("onResponse()");
            mAuthTask = null;
            showProgress(false);

            if (response.isSuccess())
            {
                Timber.d("Forgot password success");
                SuccessResponse SuccessResponse = response.body();
                showToast(SuccessResponse.getMessage());
            }
            else
            {
                int statusCode = response.code();

                if (statusCode == 422)
                {
                    ResponseBody mErrorBody = response.errorBody();

                    try
                    {
                        List<ErrorArray> errors = Utils.getErrorList(mErrorBody);
                        for (ErrorArray error : errors)
                        {
                            String field = error.getField();
                            View focusView = null;

                            switch (field)
                            {
                                case ApiFields.FIELD_EMAIL:
                                    focusView = mEmailView;
                                    mEmailView.setError(error.getMessage());
                                    break;
                            }

                            if (focusView != null)
                            {
                                focusView.requestFocus();
                            }
                        }
                    } catch (IOException e)
                    {
                        Timber.e(e, "[Err] cannot read errors from response body");
                    }
                }
                else
                {
                    Timber.d("onResponse statusCode: %d", statusCode);
                }
            }
        }

        @Override
        public void onFailure(Throwable t)
        {
            Timber.e(t, "mForgotPasswordCallback");
            showProgress(false);
            showToast("Please check your internet connection.");
            mAuthTask = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setupActionBar();

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.forgot_password || id == EditorInfo.IME_NULL)
                {
                    attemptForgotPassword();
                    return true;
                }
                return false;
            }
        });

        Button mEmailForgotPasswordButton = (Button) findViewById(R.id.email_forgot_password_button);
        mEmailForgotPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                attemptForgotPassword();
            }
        });

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form. If there are form
     * errors (invalid email, missing fields, etc.), the errors are presented and no actual login
     * attempt is made.
     */
    private void attemptForgotPassword()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else if (!isEmailValid(email))
        {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            ApiClient.ApiInterface client = ApiClient.getApiClient(null, null);
            mAuthTask = client.forgotPassword(new ForgotPasswordRequest(email));
            mAuthTask.enqueue(mForgotPasswordCallback);
        }
    }

    private boolean isEmailValid(String email)
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Shows the progress dialog.
     */
    private void showProgress(final boolean show)
    {
        if (show)
        {
            showLoadingDialog("Loading...");
        }
        else
        {
            hideLoadingDialog();
        }
    }

}

