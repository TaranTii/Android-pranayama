package it.techies.pranayama.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.ApiClient;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.token.ResetTokenCallBack;
import it.techies.pranayama.api.user.UserProfile;
import it.techies.pranayama.utils.SessionStorage;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class ProfileActivity extends AppCompatActivity
{
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int REQUEST_IMAGE_FROM_FILE = 2;

    @Bind(R.id.profile_photo_iv)
    ImageView mUserProfileImageView;

    @Bind(R.id.full_name)
    EditText mFullNameView;

    @Bind(R.id.date_of_birth)
    EditText mDateOfBirthView;

    @Bind(R.id.address)
    EditText mAddressView;

    @Bind(R.id.city)
    EditText mCityView;

    @Bind(R.id.state)
    EditText mStateView;

    @Bind(R.id.country)
    EditText mCountryView;

    @Bind(R.id.phone_number)
    EditText mPhoneNumberView;

    @Bind(R.id.timezone)
    EditText mTimezoneView;

    @Bind(R.id.loading_ll)
    LinearLayout mLoadingView;

    @Bind(R.id.reload_ll)
    LinearLayout mReloadView;

    @Bind(R.id.user_profile_form)
    ScrollView mUserProfileForm;

    @Bind(R.id.reload_btn)
    Button mReloadButton;

    @Bind(R.id.email_update_profile_button)
    Button mUpdateProfileButton;

    ApiClient.ApiInterface apiClient;

    UserProfile userProfile;

    /**
     * To store user id of customer.
     */
    private int userId;

    /**
     * User's profile photo.
     */
    private Bitmap userProfileImage = null;

    /**
     * Progress dialog.
     */
    private ProgressDialog mDialog;

    private Context mContext = this;

    /**
     * Get user profile API callback.
     */
    Callback<UserProfile> mGetUserProfileCallback = new Callback<UserProfile>()
    {
        @Override
        public void onResponse(Response<UserProfile> response, Retrofit retrofit)
        {
            showProgress(false);

            if (response.isSuccess())
            {
                userProfile = response.body();
                Timber.d("onResponse success");

            }
            else
            {
                int statusCode = response.code();

                Timber.d("onResponse failure");

                if (statusCode == 401)
                {
                    final SessionStorage settings = new SessionStorage(mContext);
                    final String email = settings.getEmail();
                    String token = settings.getAccessToken();

                    Utils.resetToken(mContext, apiClient, email, token, new ResetTokenCallBack()
                    {
                        @Override
                        public void onSuccess(String token)
                        {
                            settings.setAccessToken(token);
                            apiClient = ApiClient.getApiClient(email, token);

                            // get user's profile
                            Call<UserProfile> call = apiClient.getUserProfile(userId);
                            call.enqueue(mGetUserProfileCallback);
                        }
                    });
                }
                else
                {
                    Timber.e("[Err] cannot get user profile, statusCode: %d", statusCode);
                }
            }
        }

        @Override
        public void onFailure(Throwable t)
        {
            showProgress(false);
            showReload(true);
            Timber.e(t, "mGetUserProfileCallback");
        }
    };

    /**
     * Edit profile API callback.
     */
    Callback<EmptyResponse> mEditProfileCallback = new Callback<EmptyResponse>()
    {
        @Override
        public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
        {
            Utils.hideLoadingDialog(mDialog);

            if (response.isSuccess())
            {
                Toast.makeText(mContext, "Profile updated.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                int statusCode = response.code();
                if (statusCode == 401)
                {
                    final SessionStorage settings = new SessionStorage(mContext);
                    final String email = settings.getEmail();
                    String token = settings.getAccessToken();

                    Utils.resetToken(mContext, apiClient, email, token, new ResetTokenCallBack()
                    {
                        @Override
                        public void onSuccess(String token)
                        {
                            settings.setAccessToken(token);
                            apiClient = ApiClient.getApiClient(email, token);

                            // edit user's profile
                            Call<EmptyResponse> call = apiClient
                                    .updateUserProfile(userProfile, userId);

                            call.enqueue(mEditProfileCallback);
                        }
                    });
                }
                else
                {
                    Timber.d("[Err] unable to update user profile, statusCode: %d", statusCode);
                }
            }
        }

        @Override
        public void onFailure(Throwable t)
        {
            Utils.hideLoadingDialog(mDialog);
            Timber.e(t, "mEditProfileCallback");
            if (t != null)
            {
                String message = t.getMessage();

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException || t instanceof SocketException)
                {
                    Timber.e("Timeout occurred");
                    Toast.makeText(mContext, "Can't upload photo.\nCheck your network connection.", Toast.LENGTH_LONG)
                            .show();
                }
                else if (t instanceof IOException)
                {
                    if (message.equals("Canceled"))
                    {
                        Timber.e("onFailure() : Canceled");
                    }
                    else
                    {
                        Timber.e("onFailure() : %s", message);
                    }
                }
            }
        }
    };

    @OnClick(R.id.profile_photo_iv)
    public void selectPhoto(View v)
    {
        AlertDialog imagePickDialog = createImagePickDialog();
        imagePickDialog.show();
    }

    @OnClick(R.id.reload_btn)
    public void reload(Button v)
    {
        showReload(false);
        loadUserProfile();
    }

    @OnClick(R.id.email_update_profile_button)
    public void update(Button v)
    {
        // TODO: 09/12/2015 update profile
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mDialog = new ProgressDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SessionStorage settings = new SessionStorage(this);
        String email = settings.getEmail();
        String token = settings.getAccessToken();
        userId = settings.getUserId();

        apiClient = ApiClient.getApiClient(email, token);
        loadUserProfile();
    }

    /**
     * Shows the loading view and makes an API call to server.
     */
    private void loadUserProfile()
    {
        // show loading, hide reload layout
        showProgress(true);

        // get user's profile
        Call<UserProfile> call = apiClient.getUserProfile(userId);
        call.enqueue(mGetUserProfileCallback);
    }

    /**
     * Shows the progress UI and hides the user profile form.
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

            mUserProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mUserProfileForm.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mUserProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoadingView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            mUserProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Shows the reload UI and hides the user profile form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showReload(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mUserProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mUserProfileForm.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mUserProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mReloadView.setVisibility(show ? View.VISIBLE : View.GONE);
            mReloadView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mReloadView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mReloadView.setVisibility(show ? View.VISIBLE : View.GONE);
            mUserProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Starts the activity for selecting the file.
     */
    private void dispatchFromFileIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent
                .createChooser(intent, "Complete action using"), REQUEST_IMAGE_FROM_FILE);
    }

    /**
     * Starts the camera for taking photo of the documents
     */
    private void dispatchTakePictureIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
        else
        {
            Toast.makeText(this, "Unable to start camera", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Timber.d("onActivityResult(%d, %d)", requestCode, resultCode);

        if (resultCode != RESULT_OK)
        {
            Toast.makeText(this, "Could not receive image file", Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE)
        {
            Timber.d("REQUEST_IMAGE_CAPTURE");
            Bundle extras = data.getExtras();
            userProfileImage = (Bitmap) extras.get("data");
            setUserProfilePhoto(getImageUri(this, userProfileImage));
        }
        else if (requestCode == REQUEST_IMAGE_FROM_FILE)
        {
            Uri imageUri = data.getData();
            try
            {
                userProfileImage = MediaStore.Images.Media
                        .getBitmap(this.getContentResolver(), imageUri);

                Timber.d("REQUEST_IMAGE_FROM_FILE: %s", imageUri.toString());
                setUserProfilePhoto(imageUri);
            }
            catch (IOException e)
            {
                Timber.e(e, "onActivityResult() : REQUEST_IMAGE_FROM_FILE");
            }
        }
    }

    /**
     * Return Uri of Bitmap image.
     *
     * @param context Context
     * @param bitmap  Bitmap
     *
     * @return Uri of given Bitmap
     */
    public Uri getImageUri(Context context, Bitmap bitmap)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media
                .insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Sets the image on User profile image view.
     *
     * @param uri Image Uri
     */
    private void setUserProfilePhoto(Uri uri)
    {
        Glide.with(getApplicationContext()).load(uri).override(150, 150).centerCrop()
                .into(mUserProfileImageView);
    }

    /**
     * Create a alert imagePickDialog which asks the selectingUserImage from where he'd like to pick
     * the image
     *
     * @return AlertDialog
     */
    public AlertDialog createImagePickDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_image);
        builder.setItems(R.array.images_option_array, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                // The 'which' argument contains the index position
                // of the selected item
                if (which == 0)
                {
                    dispatchTakePictureIntent();
                }
                else if (which == 1)
                {
                    //pick from file
                    dispatchFromFileIntent();
                }
            }
        });

        return builder.create();
    }

    /**
     * Converts the given bitmap image to base64 string and sends update profile API call.
     */
    private class EncodeBitmapToBase64 extends AsyncTask<Bitmap, Integer, String>
    {
        protected String doInBackground(Bitmap... images)
        {
            return Utils.bitmapToString(images[0]);
        }

        protected void onPostExecute(String result)
        {
            userProfile.setImage(result);
            Call<EmptyResponse> call = apiClient.updateUserProfile(userProfile, userId);
            call.enqueue(mEditProfileCallback);
        }
    }

}
