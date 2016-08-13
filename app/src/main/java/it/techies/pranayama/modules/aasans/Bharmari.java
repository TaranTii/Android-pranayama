package it.techies.pranayama.modules.aasans;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import it.techies.pranayama.R;
import it.techies.pranayama.modules.aasans.base.BaseAasanActivity;
import it.techies.pranayama.modules.aasans.model.CurrentAasan;
import it.techies.pranayama.utils.FireRef;

/**
 * Created by jagdeep on 13/08/16.
 */
public class Bharmari extends BaseAasanActivity {

    public static void startActivity(Context context, @NonNull CurrentAasan currentAasan)
    {
        Intent intent = new Intent(context, Bharmari.class);
        intent.putExtra(BaseAasanActivity.KEY_CURRENT_AASAN, currentAasan);
        context.startActivity(intent);
    }

    @Override
    public String getAasanName()
    {
        return FireRef.REF_AASAN_BHARMARI;
    }

    @Override
    public String getActionBarTitle()
    {
        return "Bharmari";
    }

    @Override
    public String getAasanBenefits()
    {
        return getString(R.string.benefit_bharamri);
    }

    @Override
    public Class<?> getNextAasanClass()
    {
        return Udgeeth.class;
    }
}
