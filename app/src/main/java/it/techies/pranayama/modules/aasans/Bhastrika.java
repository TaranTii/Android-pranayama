package it.techies.pranayama.modules.aasans;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import it.techies.pranayama.R;
import it.techies.pranayama.modules.aasans.base.BaseAasanActivity;
import it.techies.pranayama.modules.aasans.model.CurrentAasan;
import it.techies.pranayama.utils.FireRef;

/**
 * Created by jagdeep on 10/08/16.
 */
public class Bhastrika extends BaseAasanActivity {

    public static void startActivity(Context context, @NonNull CurrentAasan currentAasan)
    {
        Intent intent = new Intent(context, Bhastrika.class);
        intent.putExtra(BaseAasanActivity.KEY_CURRENT_AASAN, currentAasan);
        context.startActivity(intent);
    }

    @Override
    public String getAasanName()
    {
        return FireRef.REF_AASAN_BHASTRIKA;
    }

    @Override
    public String getActionBarTitle()
    {
        return "Bhastrika";
    }

    @Override
    public String getAasanBenefits()
    {
        return getString(R.string.benefit_bhastrika);
    }

    @Override
    public Class<?> getNextAasanClass()
    {
        return KapalBhati.class;
    }

}
