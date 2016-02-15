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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.techies.pranayama.R;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.user.UserProfile;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.infrastructure.OnResetTokenSuccessCallBack;
import it.techies.pranayama.utils.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class ProfileActivity extends BaseActivity {

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

    UserProfile userProfile;

    /**
     * User's profile photo.
     */
    private Bitmap userProfileImage = null;

    /**
     * Get user profile API callback.
     */
    Callback<UserProfile> mGetUserProfileCallback = new Callback<UserProfile>() {
        @Override
        public void onResponse(Response<UserProfile> response, Retrofit retrofit)
        {
            showProgress(false);

            if (response.isSuccess())
            {
                Timber.d("onResponse success");
                userProfile = response.body();

                if (userProfile.getImage() != null && !TextUtils.isEmpty(userProfile.getImage()))
                {
                    Glide.with(mUserProfileImageView.getContext())
                            .load("http://pranayama.seobudd.com" + userProfile.getImage())
                            .override(150, 150)
                            .into(mUserProfileImageView);
                }

                mFullNameView.setText(userProfile.getFullname());
                mAddressView.setText(userProfile.getAddress1());
                mCityView.setText(userProfile.getCity());
                mStateView.setText(userProfile.getState());
                mCountryView.setText(String.valueOf(userProfile.getCountryId()));
                mDateOfBirthView.setText(userProfile.getDob());
                mPhoneNumberView.setText(userProfile.getPhone());
                mTimezoneView.setText(userProfile.getTimezone());
            }
            else
            {
                int statusCode = response.code();

                Timber.d("onResponse failure");

                if (statusCode == 401)
                {
                    resetToken(new OnResetTokenSuccessCallBack() {
                        @Override
                        public void onSuccess(String token)
                        {
                            // get user's profile
                            mAuth.setToken(ProfileActivity.this, token);
                            Call<UserProfile> call = mApiClient.getUserProfile(mAuth.getUser().getUserId());
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
    Callback<EmptyResponse> mEditProfileCallback = new Callback<EmptyResponse>() {
        @Override
        public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
        {
            Utils.hideLoadingDialog(mDialog);

            if (response.isSuccess())
            {
                hideLoadingDialog();
                showToast("Profile updated");
            }
            else
            {
                int statusCode = response.code();
                if (statusCode == 401)
                {
                    resetToken(new OnResetTokenSuccessCallBack() {
                        @Override
                        public void onSuccess(String token)
                        {
                            // edit user's profile
                            Call<EmptyResponse> call = mApiClient.updateUserProfile(userProfile, mAuth.getUser().getUserId());
                            call.enqueue(mEditProfileCallback);
                        }
                    });
                }
                else
                {
                    hideLoadingDialog();
                    Timber.d("[Err] unable to update user profile, statusCode: %d", statusCode);
                }
            }
        }

        @Override
        public void onFailure(Throwable t)
        {
            hideLoadingDialog();
            Timber.e(t, "mEditProfileCallback");
            onRetrofitFailure(t);
        }
    };

    @OnClick(R.id.profile_photo_iv)
    public void selectPhoto(View v)
    {
        showImagePickerDialog();
    }

    @OnClick(R.id.reload_btn)
    public void reload(Button v)
    {
        showReload(false);
        loadUserProfile();
    }

    @OnClick(R.id.email_update_profile_button)
    public void update(View v)
    {
        String fullName = mFullNameView.getText().toString();
        String address = mAddressView.getText().toString();
        String city = mCityView.getText().toString();
        String state = mStateView.getText().toString();
        String country = mCountryView.getText().toString();
        String dob = mDateOfBirthView.getText().toString();
        String phone = mPhoneNumberView.getText().toString();
        String timezone = mTimezoneView.getText().toString();

        View focusView = null;
        boolean cancel = false;

        if (TextUtils.isEmpty(timezone))
        {
            mTimezoneView.setError(getString(R.string.error_field_required));
            focusView = mTimezoneView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(phone))
        {
            mPhoneNumberView.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumberView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(dob))
        {
            mDateOfBirthView.setError(getString(R.string.error_field_required));
            focusView = mDateOfBirthView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(country))
        {
            mCountryView.setError(getString(R.string.error_field_required));
            focusView = mCountryView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(state))
        {
            mStateView.setError(getString(R.string.error_field_required));
            focusView = mStateView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(city))
        {
            mCityView.setError(getString(R.string.error_field_required));
            focusView = mCityView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(address))
        {
            mAddressView.setError(getString(R.string.error_field_required));
            focusView = mAddressView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(fullName))
        {
            mFullNameView.setError(getString(R.string.error_field_required));
            focusView = mFullNameView;
            cancel = true;
        }

        if (cancel)
        {
            focusView.requestFocus();
        }
        else
        {
            /*
            *   String fullName = mFullNameView.getText().toString();
                String address = mAddressView.getText().toString();
                String city = mCityView.getText().toString();
                String state = mStateView.getText().toString();
                String country = mCountryView.getText().toString();
                String dob = mDateOfBirthView.getText().toString();
                String phone = mPhoneNumberView.getText().toString();
                String timezone = mTimezoneView.getText().toString();
            * */

            userProfile.setFullname(fullName);
            userProfile.setAddress1(address);
            userProfile.setCity(city);
            userProfile.setState(state);
            // TODO: 09/12/2015 country name to id
            userProfile.setCountryId(91);
            userProfile.setDob(dob);
            userProfile.setPhone(phone);
            userProfile.setTimezone(timezone);

            Call<EmptyResponse> call = mApiClient.updateUserProfile(userProfile, mAuth.getUser().getUserId());
            Utils.showLoadingDialog(mDialog, "Updating...");
            call.enqueue(mEditProfileCallback);
        }
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
        Call<UserProfile> call = mApiClient.getUserProfile(mAuth.getUser().getUserId());
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
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mUserProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoadingView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
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
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            mUserProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mReloadView.setVisibility(show ? View.VISIBLE : View.GONE);
            mReloadView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
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
            } catch (IOException e)
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
    public void showImagePickerDialog()
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.pick_image)
                .setItems(R.array.images_option_array, new DialogInterface.OnClickListener() {
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
                })
                .create()
                .show();
    }

    /**
     * Converts the given bitmap image to base64 string and sends update profile API call.
     */
    private class EncodeBitmapToBase64 extends AsyncTask<Bitmap, Integer, String> {
        protected String doInBackground(Bitmap... images)
        {
            return Utils.bitmapToString(images[0]);
        }

        protected void onPostExecute(String result)
        {
            userProfile.setImage(result);
            Call<EmptyResponse> call = mApiClient.updateUserProfile(userProfile, mAuth.getUser().getUserId());
            call.enqueue(mEditProfileCallback);
        }
    }

}
