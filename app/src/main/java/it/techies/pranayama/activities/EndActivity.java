package it.techies.pranayama.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import it.techies.pranayama.R;
import it.techies.pranayama.api.DailyRoutine;
import it.techies.pranayama.api.EmptyResponse;
import it.techies.pranayama.api.timing.Timings;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.infrastructure.BaseBoundActivity;
import it.techies.pranayama.infrastructure.OnResetTokenSuccessCallBack;
import me.alexrs.prefs.lib.Prefs;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class EndActivity extends BaseBoundActivity {

    public static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 2;

    private DailyRoutine mDailyRoutine;
    private Uri mCurrentPhotoUri;
    private Bitmap mShareImage;

    @Bind(R.id.share_iv)
    CircleImageView mShareImageView;

    @OnClick(R.id.share_btn)
    public void onShareButtonClick(View v)
    {
        if (hasStoragePermission())
        {
            dispatchShareIntent();
        }
        else
        {
            askStoragePermission();
        }
    }

    protected void dispatchShareIntent()
    {
        Intent share = new Intent(Intent.ACTION_SEND);

        if (mShareImage != null)
        {
            try
            {
                share.setType("image/jpeg");
                Uri imageUri = convertBitmapToUri(this, mShareImage);
                share.putExtra(Intent.EXTRA_STREAM, imageUri);
            } catch (SecurityException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            share.setType("text/plain");
        }

        share.putExtra(Intent.EXTRA_TEXT, getSharingText());
        startActivity(Intent.createChooser(share, "Share..."));
    }

    public String getSharingText()
    {
        String time = mDailyRoutine.getSharingTime();
        return "I did Pranayama for " + time + " - via Pranayama app";
    }

    @OnClick(R.id.share_iv)
    public void onShareImageViewClick(View v)
    {
        showImagePickerDialog();
    }

    @OnClick(R.id.home_btn)
    public void homeButtonClick(View v)
    {
        // open the final screen
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sendDailyRouting();
    }

    private void sendDailyRouting()
    {
        mDailyRoutine = getIntent().getParcelableExtra(LauncherActivity.DAILY_ROUTINE_KEY);

        List<DailyRoutine> dailyRoutineList = new ArrayList<>();
        dailyRoutineList.add(mDailyRoutine);

        Call<EmptyResponse> call = mApiClient.setDailyRoutine(dailyRoutineList);
        call.enqueue(new Callback<EmptyResponse>() {
            @Override
            public void onResponse(Response<EmptyResponse> response, Retrofit retrofit)
            {
                if (response.isSuccess())
                {
                    // save history in prefs
                    Prefs.with(EndActivity.this).save(LoginActivity.USER_HISTORY, 1);
                    Timber.d("isSuccess()");
                }
                else
                {
                    int statusCode = response.code();
                    if (statusCode == 403)
                    {
                        // reset token
                        resetToken(new OnResetTokenSuccessCallBack() {
                            @Override
                            public void onSuccess(String token)
                            {
                                mAuth.setToken(EndActivity.this, token);
                                sendDailyRouting();
                            }
                        });
                    }
                    else
                    {
                        Timber.d("Status code %d", statusCode);
                    }

                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                onRetrofitFailure(t);
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
                            Crop.pickImage(EndActivity.this);
                        }
                    }
                })
                .create()
                .show();
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
                startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
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

        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Timber.d("REQUEST_CODE_IMAGE_CAPTURE");

            Crop.of(mCurrentPhotoUri, mCurrentPhotoUri).asSquare().start(this);

            return;
        }

        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK)
        {
            Timber.d("REQUEST_CROP");

            mShareImage = convertUriToBitmap(mCurrentPhotoUri);

            if (mShareImage != null)
            {
                setShareImageOnView(mCurrentPhotoUri);
            }
        }
    }

    private void setShareImageOnView(Uri imageUri)
    {
        Picasso.with(mShareImageView.getContext())
                .load(imageUri)
                .resizeDimen(R.dimen.profile_photo_width, R.dimen.profile_photo_height)
                .centerCrop()
                .placeholder(R.drawable.ic_aasan)
                .into(mShareImageView);
    }

    /**
     * Return Bitmap from a Uri.
     *
     * @param uri Uri
     * @return Bitmap or null
     */
    private Bitmap convertUriToBitmap(Uri uri)
    {
        try
        {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e)
        {
            showAlert("Unable to save image...");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return Uri of Bitmap image.
     *
     * @param context Context
     * @param bitmap  Bitmap
     * @return Uri of given Bitmap
     */
    private Uri convertBitmapToUri(Context context, Bitmap bitmap)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    //********************************** Runtime permissions **********************************//

    private void askStoragePermission()
    {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
        );
    }

    private boolean hasStoragePermission()
    {
        int permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );

        return permission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    dispatchShareIntent();
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showToast("Please allow storage permission to share image");
                }
                return;
            }
        }
    }

    //********************************** Runtime permissions ends *******************************//
}
