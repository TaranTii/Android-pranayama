package it.techies.pranayama.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.errors.ErrorObject;
import it.techies.pranayama.api.login.LoginRequest;
import it.techies.pranayama.api.login.LoginResponse;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.infrastructure.User;
import it.techies.pranayama.modules.launcher.LauncherActivity;
import it.techies.pranayama.utils.Utils;
import me.alexrs.prefs.lib.Prefs;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {

    public static final String USER_HISTORY = "USER_HISTORY_KEY";

    // UI references.
    @Bind(R.id.email)
    MaterialEditText mEmailView;

    @Bind(R.id.password)
    MaterialEditText mPasswordView;

    private CallbackManager callbackManager;

    @OnClick(R.id.create_account_tv)
    public void openRegister(View v)
    {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(R.id.forgot_password_tv)
    public void forgotPassword(View v)
    {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    @OnClick(R.id.email_sign_in_button)
    public void signInClick(View v)
    {
        attemptLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
                {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        initFacebookLogin();
        // checkHash();
    }

    private void checkHash()
    {
        try
        {
            PackageInfo info = getPackageManager()
                    .getPackageInfo("it.techies.pranayama", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    public void onFacebookLoginButtonClick(View v)
    {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add("email");
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.logInWithReadPermissions(this, permissions);
    }

    /**
     * Initialize the facebook login button.
     */
    private void initFacebookLogin()
    {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                Timber.d("onSuccess");
                getUsersFacebookProfile(loginResult);
            }

            @Override
            public void onCancel()
            {
                Timber.d("onCancel");
            }

            @Override
            public void onError(FacebookException exception)
            {
                Timber.d("onError");
            }
        });
    }

    protected void getUsersFacebookProfile(LoginResult loginResult)
    {
        showLoadingDialog("Signing in...");

        AccessToken accessToken = loginResult.getAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response)
            {
                FacebookRequestError error = response.getError();

                if (error != null)
                {
                    hideLoadingDialog();
                    Timber.d(error.getErrorMessage());
                }
                else
                {
                    try
                    {
                        String id = object.getString("id");
                        String email = object.getString("email");
                        sendLoginWithFacebookRequest(id, email);
                    } catch (JSONException e)
                    {
                        hideLoadingDialog();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt login with facebook information.
     *
     * @param id    Facebook user id
     * @param email Facebook email
     */
    private void sendLoginWithFacebookRequest(String id, String email)
    {
        LoginRequest request = new LoginRequest();

        request.setEmail(email);
        request.setLoginVia("fb");
        request.setFacebookId(id);

        sendLoginRequest(request);
    }

    /**
     * Attempts to sign in or register the account specified by the login form. If there are form
     * errors (invalid email, missing fields, etc.), the errors are presented and no actual login
     * attempt is made.
     */
    private void attemptLogin()
    {
        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password))
        {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else if (!isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
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

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            showLoadingDialog("Signing in...");
            sendLoginRequest(new LoginRequest(email, password, "email"));
        }
    }

    private void sendLoginRequest(final LoginRequest request)
    {
        ApiClient.ApiInterface client = ApiClient.getApiClient(null, null);
        Call<LoginResponse> call = client.login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Response<LoginResponse> response, Retrofit retrofit)
            {
                Timber.d("onResponse()");
                hideLoadingDialog();

                if (response.isSuccess())
                {
                    Timber.d("Login success");

                    // save user data
                    LoginResponse loginResponse = response.body();

                    User user = new User();
                    user.setUserId(loginResponse.getResourceId());
                    user.setEmail(request.getEmail());
                    mAuth.setUser(user);
                    mAuth.update(LoginActivity.this);
                    mAuth.setToken(LoginActivity.this, loginResponse.getToken());

                    Intent intent = new Intent(LoginActivity.this, LauncherActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // save history in prefs
                    Prefs.with(LoginActivity.this).save(USER_HISTORY, loginResponse.getHistory());

                    startActivity(intent);
                    finish();
                }
                else
                {
                    int statusCode = response.code();
                    Timber.d("onResponse statusCode: %d", response.code());

                    if (statusCode == 401)
                    {
                        try
                        {
                            ErrorObject error = Utils.getErrorObject(response.errorBody());
                            showToast(error.getMessage());
                            mPasswordView.setError(error.getMessage());
                            mPasswordView.requestFocus();
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

                                if ("Invalid fb_id.".equals(error.getMessage()) && "fb_id".equals(error.getField()))
                                {
                                    showAlert(getString(R.string.error_account_does_not_exist));
                                    LoginManager.getInstance().logOut();
                                    break;
                                }

                                if ("Please define valid email.".equals(error.getMessage()))
                                {
                                    showAlert(getString(R.string.error_account_does_not_exist));
                                    LoginManager.getInstance().logOut();
                                    return;
                                }

                                if ("Please verify your email id.".equals(error.getMessage()))
                                {
                                    showAlert(error.getMessage());
                                    return;
                                }

                                switch (field)
                                {
                                    case "email":
                                        focusView = mEmailView;
                                        mEmailView.setError(error.getMessage());
                                        break;

                                    case "login":
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
                hideLoadingDialog();
                onRetrofitFailure(t);
                Timber.e(t, "login");
                showToast("Please check your internet connection.");
            }
        });
    }

    private boolean isEmailValid(String email)
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password)
    {
        return password.length() > 4;
    }

}

