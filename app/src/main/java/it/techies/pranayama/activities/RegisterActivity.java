package it.techies.pranayama.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.ApiFields;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.register.RegisterRequest;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A register screen that offers sign up via full name, phone number, email, password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>
{
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    @Bind(R.id.full_name)
    EditText mFullNameView;

    @Bind(R.id.email)
    AutoCompleteTextView mEmailView;

    @Bind(R.id.password)
    EditText mPasswordView;

    @Bind(R.id.confirm_password)
    EditText mConfirmPasswordView;


    @Bind(R.id.email_sign_up_button)
    Button mEmailSignUpButton;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Call<EmptyResponse> mAuthTask = null;

    private Context mContext = this;

    Callback<EmptyResponse> mLoginResponseCallback = new Callback<EmptyResponse>()
    {
        @Override
        public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
        {
            Timber.d("onResponse()");
            mAuthTask = null;
            showProgress(false);

            if (response.isSuccess())
            {
                Timber.d("Register success");

                EmptyResponse registerResponse = response.body();

                // TODO: 05/11/2015 save user data and do auto login
                Toast.makeText(mContext, "Account created. Please login.", Toast.LENGTH_LONG)
                        .show();

                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
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

//                            if ("Please define valid email.".equals(error.getMessage()))
//                            {
//                                Utils.showErrorDialog(mContext, getString(R.string.error_account_does_not_exist));
                                LoginManager.getInstance().logOut();
//                                return;
//                            }

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
                    }
                    catch (IOException e)
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
            LinearLayout rootLayout = (LinearLayout) findViewById(R.id.register_root_ll);
            Snackbar.make(rootLayout, "Please check your internet connection.", Snackbar.LENGTH_LONG)
                    .show();
            mAuthTask = null;
            showProgress(false);
        }
    };

    private String mUserGender = "m";

    private CallbackManager callbackManager;

    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        setupActionBar();

        mDialog = new ProgressDialog(this);

        // Set up the login form.
        populateAutoComplete();

        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
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

        mEmailSignUpButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptRegister();
            }
        });

        initFacebookSignup();
    }

    private void populateAutoComplete()
    {
        if (!mayRequestContacts())
        {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS))
        {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener()
                    {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v)
                        {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        }
        else
        {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    private ProgressDialog mDialog;

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_READ_CONTACTS)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                populateAutoComplete();
            }
        }
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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            ApiClient.ApiInterface client = ApiClient.getApiClient(null, null);
            RegisterRequest request = new RegisterRequest(email, password, fullName, mUserGender, "email");
            mAuthTask = client.signup(request);
            mAuthTask.enqueue(mLoginResponseCallback);
        }
    }

    /**
     * Initialize the facebook login button.
     */
    private void initFacebookSignup()
    {
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        LoginManager.getInstance()
                .registerCallback(callbackManager, new FacebookCallback<LoginResult>()
                {
                    @Override
                    public void onSuccess(LoginResult loginResult)
                    {
                        Timber.d("onSuccess");
                        AccessToken accessToken = loginResult.getAccessToken();
                        GraphRequest request = GraphRequest
                                .newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback()
                                {
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

                                            }
                                            catch (JSONException e)
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

        showProgress(true);
        ApiClient.ApiInterface client = ApiClient.getApiClient(null, null);
        RegisterRequest request = new RegisterRequest(email, id, name, mUserGender, "fb");
        mAuthTask = client.signup(request);
        mAuthTask.enqueue(mLoginResponseCallback);
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
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show)
    {
        if (show)
        {
            Utils.showLoadingDialog(mDialog, "Signing up...");
        }
        else
        {
            Utils.hideLoadingDialog(mDialog);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection)
    {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery
    {
        String[] PROJECTION = {ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.IS_PRIMARY,};

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

