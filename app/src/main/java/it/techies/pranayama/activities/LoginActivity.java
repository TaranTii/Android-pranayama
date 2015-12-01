package it.techies.pranayama.activities;

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
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.ApiFields;
import it.techies.pranayama.api.errors.ErrorArray;
import it.techies.pranayama.api.errors.ErrorObject;
import it.techies.pranayama.api.login.LoginRequest;
import it.techies.pranayama.api.login.LoginResponse;
import it.techies.pranayama.utils.SessionStorage;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>
{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    public static final String USER_HISTORY = "USER_HISTORY_KEY";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private Call<LoginResponse> mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;

    private EditText mPasswordView;

    private Context mContext = this;

    private ProgressDialog mDialog;

    Callback<LoginResponse> mLoginResponseCallback = new Callback<LoginResponse>()
    {
        @Override
        public void onResponse(Response<LoginResponse> response, Retrofit retrofit)
        {
            Timber.d("onResponse()");
            mAuthTask = null;
            showProgress(false);

            if (response.isSuccess())
            {
                Timber.d("Login success");

                // save user data
                LoginResponse loginResponse = response.body();
                SessionStorage settings = new SessionStorage(mContext);

                settings.setAccessToken(loginResponse.getToken());
                settings.setUserId(loginResponse.getResourceId());
                settings.setEmail(mEmailView.getText().toString());

                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(USER_HISTORY, loginResponse.getHistory());
                startActivity(intent);

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
                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                        mPasswordView.setError(error.getMessage());
                        mPasswordView.requestFocus();
                    }
                    catch (IOException e)
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
                                case ApiFields.FIELD_EMAIL:
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
            LinearLayout rootLayout = (LinearLayout) findViewById(R.id.login_root_ll);
            Snackbar.make(rootLayout, "Please check your internet connection.", Snackbar.LENGTH_LONG)
                    .show();
            mAuthTask = null;
            showProgress(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();

        mDialog = new ProgressDialog(this);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
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

        TextView mForgotPasswordTextView = (TextView) findViewById(R.id.forgot_password_tv);
        mForgotPasswordTextView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptLogin();
            }
        });
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
     * Attempts to sign in or register the account specified by the login form. If there are form
     * errors (invalid email, missing fields, etc.), the errors are presented and no actual login
     * attempt is made.
     */
    private void attemptLogin()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            ApiClient.ApiInterface client = ApiClient.getApiClient(null, null);
            mAuthTask = client.login(new LoginRequest(email, password, "email"));
            mAuthTask.enqueue(mLoginResponseCallback);
        }
    }

    private boolean isEmailValid(String email)
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password)
    {
        return password.length() > 4;
    }

    /**
     * Shows the progress dialog.
     */
    private void showProgress(boolean show)
    {
        if(show)
        {
            Utils.showLoadingDialog(mDialog, "Signing in...");
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery
    {
        String[] PROJECTION = {ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.IS_PRIMARY,};

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

