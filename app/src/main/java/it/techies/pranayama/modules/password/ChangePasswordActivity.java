package it.techies.pranayama.modules.password;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.techies.pranayama.R;
import it.techies.pranayama.infrastructure.BaseActivity;

/**
 * A login screen that offers login via email/password.
 */
public class ChangePasswordActivity extends BaseActivity {

    @Bind(R.id.new_password)
    MaterialEditText mNewPasswordView;

    @Bind(R.id.confirm_new_password)
    MaterialEditText mConfirmNewPasswordView;

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
    }

    /**
     * Attempts to change password the account specified by the change password form. If there are
     * form errors (invalid password, missing fields, etc.), the errors are presented and no actual
     * login attempt is made.
     */
    private void attemptChangePassword()
    {
        // Reset errors.
        mNewPasswordView.setError(null);
        mConfirmNewPasswordView.setError(null);

        // Store values at the time of the login attempt.
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

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            sendChangePasswordRequest(newPassword);
        }
    }

    private void sendChangePasswordRequest(String newPassword)
    {
        showLoadingDialog("Please wait...");

        FirebaseUser user = getUser();
        Task<Void> task = user.updatePassword(newPassword);
        task.addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                hideLoadingDialog();
                showToast("Password updated");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                hideLoadingDialog();
                showToast(e.getMessage());
            }
        });
    }

}

