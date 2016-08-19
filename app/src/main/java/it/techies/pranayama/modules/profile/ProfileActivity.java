package it.techies.pranayama.modules.profile;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseActivity;
import it.techies.pranayama.models.Firebase;
import it.techies.pranayama.utils.FireRef;

public class ProfileActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener,
        View.OnClickListener, OnSuccessListener<UploadTask.TaskSnapshot>,
        OnFailureListener, OnProgressListener<UploadTask.TaskSnapshot>,
        DialogInterface.OnClickListener {

    private static final String TAG = "ProfileActivity";

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    @Bind(R.id.profile_photo_iv)
    CircleImageView mUserProfileImageView;

    @Bind(R.id.full_name_tv)
    TextView mFullNameTv;

    @Bind(R.id.date_of_birth_tv)
    TextView mDateOfBirthTv;

    @Bind(R.id.location_tv)
    TextView mLocationTv;

    @Bind(R.id.phone_number_tv)
    TextView mPhoneNumberTv;

    // Wrapper views
    @Bind(R.id.full_name_view)
    View mFullNameView;

    @Bind(R.id.date_of_birth_view)
    View mDateOfBirthView;

    @Bind(R.id.location_view)
    View mLocationView;

    @Bind(R.id.phone_number_view)
    View mPhoneNumberView;

    /**
     * User's selected photo file uri.
     */
    private Uri mCurrentPhotoUri;

    @OnClick(R.id.profile_photo_iv)
    public void selectPhoto(View v)
    {
        showImagePickerDialog();
    }

    private DatabaseReference mUserRef;
    private ValueEventListener mValueEventListener;
    private DatePickerDialog mDatePickerDialog;
    private Firebase.User.DateOfBirth mDateOfBirth;

    private StorageReference mStorageRef;
    private ProgressDialog mUploadProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFullNameView.setOnClickListener(this);
        mDateOfBirthView.setOnClickListener(this);
        mLocationView.setOnClickListener(this);
        mPhoneNumberView.setOnClickListener(this);

        mUserRef = FirebaseDatabase.getInstance()
                .getReference(FireRef.REF_USERS)
                .child(getUid());

        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        mStorageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage_ref));

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange() called with: " + "dataSnapshot = [" + dataSnapshot + "]");

                Firebase.User user = dataSnapshot.getValue(Firebase.User.class);
                if (user != null) {
                    // user full name
                    mFullNameTv.setText(user.displayName);

                    // if user photo exists
                    if (user.photoUrl != null) {
                        setUserProfilePhoto(user.photoUrl);
                    }

                    // if user does not have date of birth set
                    if (user.dateOfBirth == null) {
                        mDateOfBirth = new Firebase.User.DateOfBirth();
                    } else {
                        mDateOfBirth = user.dateOfBirth;
                    }

                    // if date of birth is empty
                    if (mDateOfBirth.isEmpty()) {
                        mDateOfBirthTv.setText(R.string.n_a);
                    } else {
                        mDateOfBirthTv.setText(mDateOfBirth.toString());
                    }

                    // user location
                    if (!TextUtils.isEmpty(user.location)) {
                        mLocationTv.setText(user.location);
                    } else {
                        mLocationTv.setText(R.string.n_a);
                    }

                    // user phone number
                    if (!TextUtils.isEmpty(user.phone)) {
                        mPhoneNumberTv.setText(user.phone);
                    } else {
                        mPhoneNumberTv.setText(R.string.n_a);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled() called with: " + databaseError.getDetails());
            }
        };
        mUserRef.addValueEventListener(mValueEventListener);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mUserRef != null && mValueEventListener != null) {
            mUserRef.removeEventListener(mValueEventListener);
        }

        if (mUploadProgressDialog != null) {
            mUploadProgressDialog.dismiss();
            mUploadProgressDialog = null;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        if (mDateOfBirth != null) {
            mDateOfBirth.year = year;
            mDateOfBirth.month = monthOfYear + 1;
            mDateOfBirth.day = dayOfMonth;

            mDateOfBirthTv.setText(mDateOfBirth.toString());

            if (!mDateOfBirth.isEmpty()) {
                mUserRef.child(Firebase.User.DateOfBirth.KEY)
                        .setValue(mDateOfBirth);
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.full_name_view:
                showNamePickerDialog();
                break;

            case R.id.date_of_birth_view:
                showDatePickerDialog();
                break;

            case R.id.location_view:
                showLocationPickerDialog();
                break;

            case R.id.phone_number_view:
                showPhoneNumberPickerDialog();
                break;
        }
    }

    /**
     * Shows a date picker dialog for user date of birth.
     */
    protected void showDatePickerDialog()
    {
        if (mDatePickerDialog == null) {
            if (mDateOfBirth != null && !mDateOfBirth.isEmpty()) {
                mDatePickerDialog = new DatePickerDialog(
                        this,
                        this,
                        mDateOfBirth.year,
                        mDateOfBirth.month - 1,
                        mDateOfBirth.day);
            } else {
                Calendar calendar = Calendar.getInstance();
                mDatePickerDialog = new DatePickerDialog(
                        this,
                        this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
            }
        }

        mDatePickerDialog.show();
    }

    /**
     * Shows a dialog to enter user full name.
     */
    private void showNamePickerDialog()
    {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_full_name, null);
        final EditText fullName = (EditText) dialogView.findViewById(R.id.full_name_et);
        fullName.setText(mFullNameTv.getText());

        new AlertDialog.Builder(this)
                .setTitle(R.string.hint_full_name)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        String name = fullName.getText().toString().trim();
                        if (!TextUtils.isEmpty(name)) {
                            mUserRef.child("displayName").setValue(name);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                })
                .create()
                .show();
    }

    /**
     * Shows a dialog to enter user location.
     */
    private void showLocationPickerDialog()
    {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_location, null);
        final EditText editText = (EditText) dialogView.findViewById(R.id.location_et);

        String location = mLocationTv.getText().toString();
        // if the location is not available
        if (!TextUtils.equals(location, getString(R.string.n_a))) {
            editText.setText(location);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.hint_location)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        String location = editText.getText().toString().trim();
                        if (!TextUtils.isEmpty(location)) {
                            mUserRef.child("location").setValue(location);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                })
                .create()
                .show();
    }

    /**
     * Shows a dialog to enter user phone number.
     */
    private void showPhoneNumberPickerDialog()
    {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_phone, null);
        final EditText editText = (EditText) dialogView.findViewById(R.id.phone_number_et);

        String phone = mPhoneNumberTv.getText().toString();
        // if the phone number is not available
        if (!TextUtils.equals(phone, getString(R.string.n_a))) {
            editText.setText(phone);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.hint_phone_number)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        String phone = editText.getText().toString().trim();
                        if (!TextUtils.isEmpty(phone)) {
                            mUserRef.child("phone").setValue(phone);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                })
                .create()
                .show();
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

        if (storageDir == null) {
            storageDir = getCacheDir();
            Log.d(TAG, "createTempFile: Using getCacheDir()");
        } else {
            Log.d(TAG, "createTempFile: Using getExternalCacheDir()");
        }

        return File.createTempFile(imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */);
    }

    /**
     * Starts the camera for taking photo of the documents
     */
    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createTempFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                showToast("Unable to create a temporary file...");
                e.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCurrentPhotoUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: REQUEST_PICK");

            try {
                File tempFile = createTempFile();
                mCurrentPhotoUri = Uri.fromFile(tempFile);
                Crop.of(data.getData(), mCurrentPhotoUri).asSquare().start(this);
            } catch (IOException e) {
                showToast("Unable to create a temporary file...");
                e.printStackTrace();
            }

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: REQUEST_CODE_IMAGE_CAPTURE");

            Crop.of(mCurrentPhotoUri, mCurrentPhotoUri).asSquare().start(this);

        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: REQUEST_CROP");
            uploadImageToFirebase(mCurrentPhotoUri);
        }
    }

    private void uploadImageToFirebase(Uri currentPhotoUri)
    {
        // Create a child reference
        StorageReference imageRef = mStorageRef.child("users").child(getUid())
                .child("profilePicture.jpeg");

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();

        UploadTask task = imageRef.putFile(currentPhotoUri, metadata);

        task.addOnSuccessListener(this, this);
        task.addOnFailureListener(this, this);
        task.addOnProgressListener(this, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // If there's an upload in progress, save the reference so you can query it later
        if (mStorageRef != null) {
            outState.putString("reference", mStorageRef.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was an upload in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }

        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Get the task monitoring the upload
        UploadTask task = getPendingUploadTask(mStorageRef);

        if (task != null) {
            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, this);
            task.addOnFailureListener(this, this);
            task.addOnProgressListener(this, this);
        }
    }

    private UploadTask getPendingUploadTask(StorageReference mStorageRef)
    {
        // Find all UploadTasks under this StorageReference
        List<UploadTask> tasks = mStorageRef.getActiveUploadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the upload
            return tasks.get(0);
        } else {
            return null;
        }
    }

    /**
     * Sets the image on user profile image view.
     *
     * @param url Image url
     */
    private void setUserProfilePhoto(String url)
    {
        Picasso.with(mUserProfileImageView.getContext())
                .load(url)
                .resizeDimen(R.dimen.profile_photo_width, R.dimen.profile_photo_height)
                .centerCrop()
                .placeholder(R.drawable.ic_aasan)
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
                        if (which == 0) {
                            dispatchTakePictureIntent();
                        } else if (which == 1) {
                            //pick from file
                            Crop.pickImage(ProfileActivity.this);
                        }
                    }
                }).create().show();
    }

    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
    {
        Uri downloadUrl = taskSnapshot.getDownloadUrl();
        Log.d(TAG, "onSuccess: downloadUrl: " + downloadUrl);

        if (downloadUrl != null) {
            mUserRef.child("photoUrl").setValue(downloadUrl.toString());
        }

        if (mUploadProgressDialog != null) {
            mUploadProgressDialog.dismiss();
            mUploadProgressDialog = null;
        }
    }

    @Override
    public void onFailure(@NonNull Exception e)
    {
        Log.e(TAG, "onFailure: e: ", e);
        showToast(e.getMessage());

        if (mUploadProgressDialog != null) {
            mUploadProgressDialog.dismiss();
            mUploadProgressDialog = null;
        }
    }

    @Override
    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
    {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        Log.d(TAG, "onProgress: " + "Upload is " + progress + "% done");

        if (mUploadProgressDialog == null) {
            initializeProgressDialog();
        }

        // update upload progress
        mUploadProgressDialog.setProgress((int) progress);
    }

    protected void initializeProgressDialog()
    {
        mUploadProgressDialog = new ProgressDialog(this);
        mUploadProgressDialog.setMessage("Uploading photo");
        mUploadProgressDialog.setCanceledOnTouchOutside(false);
        mUploadProgressDialog.setIndeterminate(false);
        mUploadProgressDialog.setCancelable(false);
        mUploadProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.cancel), this);
        mUploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mUploadProgressDialog.setMax(100);
        mUploadProgressDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i)
    {
        // Get the task monitoring the upload
        UploadTask task = getPendingUploadTask(mStorageRef);
        if (task != null && task.isInProgress()) {
            task.cancel();
        }
    }
}
