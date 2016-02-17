package it.techies.pranayama.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
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
    CircleImageView mUserProfileImageView;

    @Bind(R.id.full_name)
    MaterialEditText mFullNameView;

    @Bind(R.id.date_of_birth)
    MaterialEditText mDateOfBirthView;

    @Bind(R.id.address)
    MaterialEditText mAddressView;

    @Bind(R.id.city)
    MaterialEditText mCityView;

    @Bind(R.id.state)
    MaterialEditText mStateView;

    @Bind(R.id.country)
    MaterialEditText mCountryView;

    @Bind(R.id.phone_number)
    MaterialEditText mPhoneNumberView;

    @Bind(R.id.timezone)
    MaterialEditText mTimezoneView;

    @Bind(R.id.loading_ll)
    View mLoadingView;

    @Bind(R.id.reload_ll)
    View mReloadView;

    @Bind(R.id.user_profile_form)
    View mUserProfileForm;

    /**
     * User profile model.
     */
    private UserProfile userProfile;


    /**
     * User's profile photo.
     */
    Bitmap userProfileImage;

    /**
     * User's selected photo file uri.
     */
    Uri mCurrentPhotoUri;

    @OnClick(R.id.profile_photo_iv)
    public void selectPhoto(View v)
    {
        showImagePickerDialog();
    }

    @OnClick(R.id.reload_btn)
    public void reload(View v)
    {
        showReload(false);
        loadUserProfile();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadUserProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_update)
        {
            updateProfile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks the form validation and updates the user profile.
     */
    private void updateProfile()
    {
        String fullName = mFullNameView.getText().toString().trim();
        String address = mAddressView.getText().toString().trim();
        String city = mCityView.getText().toString().trim();
        String state = mStateView.getText().toString().trim();
        String country = mCountryView.getText().toString().trim();
        String dob = mDateOfBirthView.getText().toString().trim();
        String phone = mPhoneNumberView.getText().toString().trim();
        String timezone = mTimezoneView.getText().toString().trim();

        View focusView = null;
        boolean cancel = false;

        if (TextUtils.isEmpty(timezone))
        {
            mTimezoneView.setError(getString(R.string.error_field_required));
            focusView = mTimezoneView;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone))
        {
            mPhoneNumberView.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumberView;
            cancel = true;
        }

        if (TextUtils.isEmpty(country))
        {
            mCountryView.setError(getString(R.string.error_field_required));
            focusView = mCountryView;
            cancel = true;
        }

        if (TextUtils.isEmpty(state))
        {
            mStateView.setError(getString(R.string.error_field_required));
            focusView = mStateView;
            cancel = true;
        }

        if (TextUtils.isEmpty(city))
        {
            mCityView.setError(getString(R.string.error_field_required));
            focusView = mCityView;
            cancel = true;
        }

        if (TextUtils.isEmpty(address))
        {
            mAddressView.setError(getString(R.string.error_field_required));
            focusView = mAddressView;
            cancel = true;
        }

        if (TextUtils.isEmpty(dob))
        {
            mDateOfBirthView.setError(getString(R.string.error_field_required));
            focusView = mDateOfBirthView;
            cancel = true;
        }

        if (TextUtils.isEmpty(fullName))
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
                String fullName = mFullNameView.getText().toString();
                String address = mAddressView.getText().toString();
                String city = mCityView.getText().toString();
                String state = mStateView.getText().toString();
                String country = mCountryView.getText().toString();
                String dob = mDateOfBirthView.getText().toString();
                String phone = mPhoneNumberView.getText().toString();
                String timezone = mTimezoneView.getText().toString();
            */

            userProfile.setFullname(fullName);
            userProfile.setAddress1(address);
            userProfile.setCity(city);
            userProfile.setState(state);
            // TODO: 09/12/2015 convert country name to id
            userProfile.setCountryId(91);
            userProfile.setDob(dob);
            userProfile.setPhone(phone);
            userProfile.setTimezone(timezone);

            showLoadingDialog("Updating...");
            new EncodeBitmapToBase64().execute(userProfileImage);
        }
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
        call.enqueue(new Callback<UserProfile>() {
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
                        setUserProfilePhoto(userProfile.getImage().trim());
                    }

                    if (userProfile.getFullname() != null)
                    {
                        mFullNameView.setText(userProfile.getFullname().trim());
                    }

                    if (userProfile.getAddress1() != null)
                    {
                        mAddressView.setText(userProfile.getAddress1().trim());
                    }

                    if (userProfile.getCity() != null)
                    {
                        mCityView.setText(userProfile.getCity().trim());
                    }

                    if (userProfile.getState() != null)
                    {
                        mStateView.setText(userProfile.getState().trim());
                    }

                    if (userProfile.getCountryId() != null)
                    {
                        mCountryView.setText(String.valueOf(userProfile.getCountryId()).trim());
                    }

                    if (userProfile.getDob() != null)
                    {
                        mDateOfBirthView.setText(userProfile.getDob().trim());
                    }

                    if (userProfile.getPhone() != null)
                    {
                        mPhoneNumberView.setText(userProfile.getPhone().trim());
                    }

                    if (userProfile.getTimezone() != null)
                    {
                        mTimezoneView.setText(userProfile.getTimezone().trim());
                    }
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
                                mAuth.setToken(ProfileActivity.this, token);
                                loadUserProfile();
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
                onRetrofitFailure(t);
                showProgress(false);
                showReload(true);
            }
        });
    }

    /**
     * Shows the progress UI and hides the user profile form.
     */
    private void showProgress(final boolean show)
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

    /**
     * Shows the reload UI and hides the user profile form.
     */
    private void showReload(final boolean show)
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

    /**
     * Creates a temporary image file.
     *
     * @return File
     * @throws IOException
     */
    private File createTempFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalCacheDir();

        if (storageDir == null)
        {
            storageDir = getCacheDir();
            Timber.d("Using getCacheDir()");
        }
        else
        {
            Timber.d("Using getExternalCacheDir()");
        }

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /**
     * Starts the camera for taking photo of the documents
     */
    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File photoFile = null;
            try
            {
                photoFile = createTempFile();
            } catch (IOException e)
            {
                // Error occurred while creating the File
                showToast("Unable to create a temporary file...");
                e.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                mCurrentPhotoUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Timber.d("onActivityResult(%d, %d)", requestCode, resultCode);

        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK)
        {
            Timber.d("REQUEST_PICK");

            File tempFile;
            try
            {
                tempFile = createTempFile();
                mCurrentPhotoUri = Uri.fromFile(tempFile);
                Crop.of(data.getData(), mCurrentPhotoUri).asSquare().start(this);

            } catch (IOException e)
            {
                showToast("Unable to create a temporary file...");
                e.printStackTrace();
            }

            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Timber.d("REQUEST_IMAGE_CAPTURE");

            Crop.of(mCurrentPhotoUri, mCurrentPhotoUri).asSquare().start(this);

            return;
        }

        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK)
        {
            Timber.d("REQUEST_CROP");

            try
            {
                userProfileImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCurrentPhotoUri);
                setUserProfilePhoto(mCurrentPhotoUri);
            } catch (IOException e)
            {
                showAlert("Unable to save image...");
                e.printStackTrace();
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
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Sets the image on user profile image view.
     *
     * @param uri Image Uri
     */
    private void setUserProfilePhoto(Uri uri)
    {
        Picasso.with(mUserProfileImageView.getContext())
                .load(uri)
                .resizeDimen(R.dimen.profile_photo_width, R.dimen.profile_photo_height)
                .centerCrop()
                .into(mUserProfileImageView);
    }

    /**
     * Sets the image on user profile image view.
     *
     * @param url Image url
     */
    private void setUserProfilePhoto(String url)
    {
        Picasso.with(mUserProfileImageView.getContext())
                .load("http://pranayama.seobudd.com" + url)
                .resizeDimen(R.dimen.profile_photo_width, R.dimen.profile_photo_height)
                .centerCrop()
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
                            Crop.pickImage(ProfileActivity.this);
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
            sendEditProfileRequest();
        }
    }

    /**
     * Send edit user profile API request.
     */
    private void sendEditProfileRequest()
    {
        Call<EmptyResponse> call = mApiClient.updateUserProfile(userProfile, mAuth.getUser().getUserId());
        call.enqueue(new Callback<EmptyResponse>() {
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
                                mAuth.setToken(ProfileActivity.this, token);
                                sendEditProfileRequest();
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
                onRetrofitFailure(t);
                hideLoadingDialog();
            }
        });
    }

}
