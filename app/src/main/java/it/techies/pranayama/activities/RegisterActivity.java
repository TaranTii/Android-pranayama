package it.techies.pranayama.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.ApiFields;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.register.RegisterRequest;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;


/**
 * A register screen that offers sign up via full name, phone number, email, password.
 */
public class RegisterActivity extends BaseActivity {

    @Bind(R.id.full_name)
    MaterialEditText mFullNameView;

    @Bind(R.id.email)
    MaterialEditText mEmailView;

    @Bind(R.id.password)
    MaterialEditText mPasswordView;

    @Bind(R.id.confirm_password)
    MaterialEditText mConfirmPasswordView;

    @Bind(R.id.email_sign_up_button)
    Button mEmailSignUpButton;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Call<EmptyResponse> mAuthTask = null;

    private Context mContext = this;

    private String mUserGender = "m";

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.register || id == EditorInfo.IME_NULL)
                {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                attemptRegister();
            }
        });

        initFacebookSignup();
    }


    /**
     * Attempts to register the account specified by the login form. If there are form errors
     * (invalid email, missing fields, etc.), the errors are presented and no actual register
     * attempt is made.
     */
    private void attemptRegister()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mFullNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String fullName = mFullNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for confirm password validation
        if (TextUtils.isEmpty(confirmPassword))
        {
            mConfirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = mConfirmPasswordView;
            cancel = true;
        }
        else if (!TextUtils.equals(password, confirmPassword))
        {
            mConfirmPasswordView.setError(getString(R.string.error_confirm_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password))
        {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

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

        // Check for name validation
        if (TextUtils.isEmpty(fullName))
        {
            mFullNameView.setError(getString(R.string.error_field_required));
            focusView = mFullNameView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            RegisterRequest request = new RegisterRequest(email, password, fullName, mUserGender, "email");
            sendRegisterRequest(request);
        }
    }

    /**
     * Initialize the facebook login button.
     */
    private void initFacebookSignup()
    {
        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        LoginManager.getInstance()
                .registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult)
                    {
                        Timber.d("onSuccess");
                        AccessToken accessToken = loginResult.getAccessToken();
                        GraphRequest request = GraphRequest
                                .newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response)
                                    {
                                        FacebookRequestError error = response.getError();

                                        if (error != null)
                                        {
                                            Timber.d(error.getErrorMessage());
                                        }
                                        else
                                        {
                                            try
                                            {
                                                String id = object.getString("id");
                                                String email = object.getString("email");
                                                String gender = object.getString("gender");
                                                String name = object.getString("name");

                                                /*Timber.d("Name - %s", object.getString("name"));
                                                Timber.d("ID - %s", object.getString("id"));
                                                Timber.d("Email - %s", object.getString("email"));
                                                Timber.d("Gender - %s", object.getString("gender"));
                                                */

                                                signUpWithFacebook(id, email, name, gender);

                                            } catch (JSONException e)
                                            {
                                                e.printStackTrace();
                                            }

                                        }
                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, name, email, gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel()
                    {
                        // App code
                        Timber.d("onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception)
                    {
                        // App code
                        Timber.d("onError");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Do the sign up with given data from Facebook.
     *
     * @param id     User's Facebook ID
     * @param email  User's email
     * @param name   User's name
     * @param gender User's gender
     */
    private void signUpWithFacebook(String id, String email, String name, String gender)
    {
        if ("male".equals(gender))
        {
            mUserGender = "m";
            RadioButton male = (RadioButton) findViewById(R.id.male);
            male.setChecked(true);
        }
        else if ("female".equals(gender))
        {
            mUserGender = "f";
            RadioButton female = (RadioButton) findViewById(R.id.female);
            female.setChecked(true);
        }

        RegisterRequest request = new RegisterRequest(email, id, name, mUserGender, "fb");
        sendRegisterRequest(request);
    }

    private void sendRegisterRequest(RegisterRequest request)
    {
        showLoadingDialog("Please wait...");
        ApiClient.ApiInterface client = ApiClient.getApiClient(null, null);
        mAuthTask = client.signup(request);
        mAuthTask.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
            {
                Timber.d("onResponse()");
                mAuthTask = null;
                hideLoadingDialog();

                if (response.isSuccess())
                {
                    Timber.d("Register success");

                    new AlertDialog.Builder(RegisterActivity.this)
                            .setCancelable(false)
                            .setMessage("Account created successfully. Please login.")
                            .setPositiveButton(R.string.action_login, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                    Intent intent = new Intent(mContext, LoginActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .create()
                            .show();

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

                                LoginManager.getInstance().logOut();

                                switch (field)
                                {
                                    case ApiFields.FIELD_FULL_NAME:
                                        focusView = mFullNameView;
                                        mFullNameView.setError(error.getMessage());
                                        break;
                                    case ApiFields.FIELD_EMAIL:
                                        focusView = mEmailView;
                                        mEmailView.setError(error.getMessage());
                                        break;
                                    case ApiFields.FIELD_PASSWORD:
                                        focusView = mPasswordView;
                                        mPasswordView.setError(error.getMessage());
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
                Timber.e(t, "mLoginResponseCallback");
                showToast("Please check your internet connection.");
                mAuthTask = null;
            }
        });
    }

    /**
     * Handles the gender checkbox click.
     *
     * @param view View
     */
    public void onGenderChecked(View view)
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId())
        {
            case R.id.male:
                if (checked)
                {
                    mUserGender = "m";
                }
                break;
            case R.id.female:
                if (checked)
                {
                    mUserGender = "f";
                }
                break;
        }
    }

    private boolean isEmailValid(String email)
    {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        return password.length() > 4;
    }

}

