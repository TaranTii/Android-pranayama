package it.techies.pranayama.modules.aasans;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import it.techies.pranayama.R;
import it.techies.pranayama.modules.aasans.base.BaseAasanActivity;
import it.techies.pranayama.modules.aasans.model.CurrentAasan;
import it.techies.pranayama.utils.FireRef;

/**
 * Created by jagdeep on 11/08/16.
 */
public class KapalBhati extends BaseAasanActivity {

    public static void startActivity(Context context, @NonNull CurrentAasan currentAasan)
    {
        Intent intent = new Intent(context, KapalBhati.class);
        intent.putExtra(BaseAasanActivity.KEY_CURRENT_AASAN, currentAasan);
        context.startActivity(intent);
    }

    @Override
    public String getAasanName()
    {
        return FireRef.REF_AASAN_KAPALBHATI;
    }

    @Override
    public String getActionBarTitle()
    {
        return "Kapalbhati";
    }

    @Override
    public String getAasanBenefits()
    {
        return getString(R.string.benefit_kapalbhati);
    }

    @Override
    public Class<?> getNextAasanClass()
    {
        return Bahaya.class;
    }

}
