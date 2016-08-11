package it.techies.pranayama.infrastructure;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.techies.pranayama.MyApplication;
import it.techies.pranayama.R;
import it.techies.pranayama.modules.splash.SplashActivity;

/**
 * Created by jagdeep on 28/01/16.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected MyApplication mApplication;
    protected ProgressDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mApplication = (MyApplication) getApplication();
    }

    @Override
    public void startActivity(Intent intent)
    {
        super.startActivity(intent);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    /**
     * Shows the loading dialog
     */
    protected void showLoadingDialog(String message)
    {
        if (mDialog == null)
        {
            mDialog = new ProgressDialog(this);
        }

        if (!mDialog.isShowing())
        {
            mDialog.show();
            mDialog.setMessage(message);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
        }
    }

    /**
     * Hides the loading dialog
     */
    protected void hideLoadingDialog()
    {
        if (mDialog != null && mDialog.isShowing())
        {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Logout the user and starts the Splash activity.
     */
    protected void logoutUser()
    {
        AuthUI.getInstance().signOut(this);

        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected void showAlert(String message)
    {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    protected String getUid()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    protected FirebaseUser getUser()
    {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

}

