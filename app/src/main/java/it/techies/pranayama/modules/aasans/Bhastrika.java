package it.techies.pranayama.modules.aasans;

import android.content.Intent;

import it.techies.pranayama.R;
import it.techies.pranayama.utils.FireRef;

/**
 * Created by jagdeep on 10/08/16.
 */
public class Bhastrika extends BaseAasanActivity {

    @Override
    public void startNextAasan()
    {
        Intent intent = new Intent(this, AasanActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public String getAasanName()
    {
        return FireRef.REF_AASAN_BHASTRIKA;
    }

    @Override
    public String getActionBarTitle()
    {
        return "Bhastrike";
    }

    @Override
    public String getAasanBenefits()
    {
        return getString(R.string.benefit_bhastrika);
    }
}
