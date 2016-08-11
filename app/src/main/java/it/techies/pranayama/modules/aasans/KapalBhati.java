package it.techies.pranayama.modules.aasans;

import android.content.Intent;
import android.support.annotation.NonNull;

import it.techies.pranayama.R;
import it.techies.pranayama.utils.FireRef;

/**
 * Created by jagdeep on 11/08/16.
 */
public class KapalBhati extends BaseAasanActivity {

    @Override
    public String getActionBarTitle()
    {
        return "KapalBhati";
    }

    @Override
    public void startNextAasan(@NonNull CurrentAasan currentAasan)
    {
        Intent intent = new Intent(this, KapalBhati.class);
        intent.putExtra(BaseAasanActivity.KEY_CURRENT_AASAN, currentAasan);
        startActivity(intent);
        finish();
    }

    @Override
    public String getAasanName()
    {
        return FireRef.REF_AASAN_KAPALBHATI;
    }

    @Override
    public String getAasanBenefits()
    {
        return getString(R.string.benefit_kapalbhati);
    }
}
