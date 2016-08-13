package it.techies.pranayama.infrastructure;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import it.techies.pranayama.R;
import it.techies.pranayama.modules.password.ChangePasswordActivity;
import it.techies.pranayama.activities.HistoryActivity;
import it.techies.pranayama.activities.ProfileActivity;
import it.techies.pranayama.activities.SettingsActivity;
import it.techies.pranayama.modules.setup.SetupActivity;

/**
 * Created by jagdeep on 23/02/16.
 */
public class BaseDrawerActivity extends BaseBoundActivity implements Drawer.OnDrawerItemClickListener {

    protected static final int REQUEST_CODE_SETUP = 1;

    // save our header or result
    protected Drawer result = null;

    protected void setupDrawer(Toolbar toolbar, Bundle savedInstanceState)
    {
        // Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_home)
                                .withIcon(GoogleMaterial.Icon.gmd_home)
                                .withSelectable(false),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_history)
                                .withIcon(GoogleMaterial.Icon.gmd_history)
                                .withSelectable(false),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_profile)
                                .withIcon(GoogleMaterial.Icon.gmd_person)
                                .withSelectable(false),

                        new DividerDrawerItem(),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_change_password)
                                .withIcon(GoogleMaterial.Icon.gmd_lock)
                                .withSelectable(false),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_general_settings)
                                .withIcon(GoogleMaterial.Icon.gmd_settings)
                                .withSelectable(false),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_schedule_setup)
                                .withIcon(GoogleMaterial.Icon.gmd_alarm)
                                .withSelectable(false),

                        new DividerDrawerItem(),

                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_logout)
                                .withIcon(GoogleMaterial.Icon.gmd_power_settings_new)
                                .withSelectable(false)

                )
                // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(this)
                .withSavedInstance(savedInstanceState)
                .build();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed()
    {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen())
        {
            result.closeDrawer();
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
    {
        if (drawerItem != null && drawerItem instanceof Nameable)
        {
            switch (position)
            {
                case 0:
                    break;
                case 1:
                    startActivity(new Intent(this, HistoryActivity.class));
                    break;
                case 2:
                    startActivity(new Intent(this, ProfileActivity.class));
                    break;
                case 4:
                    startActivity(new Intent(this, ChangePasswordActivity.class));
                    break;
                case 5:
                    startActivity(new Intent(this, SettingsActivity.class));
                    break;
                case 6:
                    startActivity(new Intent(this, SetupActivity.class));
                    break;
                case 8:
                    logoutUser();
                    break;
            }

            result.closeDrawer();
        }
        return false;
    }
}
