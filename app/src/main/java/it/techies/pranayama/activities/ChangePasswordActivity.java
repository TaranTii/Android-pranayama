package it.techies.pranayama.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiFields;
import it.techies.pranayama.api.SuccessResponse;
import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.errors.ErrorObject;
import it.techies.pranayama.api.login.ChangePasswordRequest;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * A login screen that offers login via email/password.
 */
public class ChangePasswordActivity extends BaseActivity {

    @Bind(R.id.current_password)
    MaterialEditText mCurrentPasswordView;

    @Bind(R.id.new_password)
    MaterialEditText mNewPasswordView;

    @Bind(R.id.confirm_new_password)
    MaterialEditText mConfirmNewPasswordView;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Call<SuccessResponse> mAuthTask = null;

    private View mProgressView;

    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mConfirmNewPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.action_change_password || id == EditorInfo.IME_NULL)
                {
                    attemptChangePassword();
                    return true;
                }
                return false;
            }
        });

        Button mChangePasswordButton = (Button) findViewById(R.id.change_password_button);
        mChangePasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                attemptChangePassword();
            }
        });

        mLoginFormView = findViewById(R.id.change_password_form);
        mProgressView = findViewById(R.id.change_password_progress);
    }

    /**
     * Attempts to change password the account specified by the change password form. If there are
     * form errors (invalid password, missing fields, etc.), the errors are presented and no actual
     * login attempt is made.
     */
    private void attemptChangePassword()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mCurrentPasswordView.setError(null);
        mNewPasswordView.setError(null);
        mConfirmNewPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String currentPassword = mCurrentPasswordView.getText().toString().trim();
        String newPassword = mNewPasswordView.getText().toString().trim();
        String confirmNewPassword = mConfirmNewPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // confirm password password matches new password
        if (!TextUtils.equals(newPassword, confirmNewPassword))
        {
            mConfirmNewPasswordView.setError(getString(R.string.error_confirm_password));
            focusView = mConfirmNewPasswordView;
            cancel = true;
        }

        // confirm password should not be empty
        if (TextUtils.isEmpty(confirmNewPassword))
        {
            mConfirmNewPasswordView.setError(getString(R.string.error_field_required));
            focusView = mConfirmNewPasswordView;
            cancel = true;
        }

        // confirm password should not be empty
        if (TextUtils.isEmpty(newPassword))
        {
            mNewPasswordView.setError(getString(R.string.error_field_required));
            focusView = mNewPasswordView;
            cancel = true;
        }

        // current password should not be empty
        if (TextUtils.isEmpty(currentPassword))
        {
            mCurrentPasswordView.setError(getString(R.string.error_field_required));
            focusView = mCurrentPasswordView;
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
            sendChangePasswordRequest(currentPassword, newPassword);
        }
    }

    private void sendChangePasswordRequest(String currentPassword, String newPassword)
    {
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        mAuthTask = mApiClient.changePassword(
                new ChangePasswordRequest(currentPassword, newPassword),
                mAuth.getUser().getUserId()
        );

        mAuthTask.enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Response<SuccessResponse> response, Retrofit retrofit)
            {
                showProgress(false);
                mAuthTask = null;

                if (response.isSuccess())
                {
                    showToast(getString(R.string.password_changed_success_message));
                    finish();
                }
                else
                {
                    int statusCode = response.code();
                    Timber.d("[Err] could not change password, statusCode: %d", statusCode);

                    if (statusCode == 401)
                    {
                        try
                        {
                            ErrorObject error = Utils.getErrorObject(response.errorBody());
                            showToast(error.getMessage());
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (statusCode == 422)
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
                                    case ApiFields.FIELD_NEW_PASSWORD:
                                        focusView = mNewPasswordView;
                                        mNewPasswordView.setError(error.getMessage());
                                        break;
                                    case ApiFields.FIELD_CURRENT_PASSWORD:
                                        focusView = mCurrentPasswordView;
                                        mCurrentPasswordView.setError(error.getMessage());
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
                Timber.e(t, "mChangePasswordCallback");
                showProgress(false);
                mAuthTask = null;
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

