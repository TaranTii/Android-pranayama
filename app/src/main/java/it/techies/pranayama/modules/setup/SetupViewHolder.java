package it.techies.pranayama.modules.setup;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import it.techies.pranayama.R;
import it.techies.pranayama.models.FirebaseSchedule;

/**
 * Created by jagdeep on 29/07/16.
 */
public class SetupViewHolder extends RecyclerView.ViewHolder {

    public TextView mSets;
    public TextView mAasanName;
    public TextView mAasanTime;
    public View mItemView;

    public SetupViewHolder(View itemView)
    {
        super(itemView);

        mAasanName = (TextView) itemView.findViewById(R.id.aasan_name_tv);
        mAasanTime = (TextView) itemView.findViewById(R.id.time_tv);
        mSets = (TextView) itemView.findViewById(R.id.set_tv);
        mItemView = itemView;
    }

    public void bindToPost(FirebaseSchedule model, View.OnClickListener itemClickListener)
    {
        String time = String.format(Locale.getDefault(), "Time: %02d:%02d", model.getMinutes(), model.getSeconds());
        mAasanTime.setText(time);

        if (model.type.equals(FirebaseSchedule.TYPE_BREAK))
        {
            mAasanName.setVisibility(View.GONE);
            mSets.setText(model.name);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                mSets.setTextAppearance(R.style.AppTheme_TextAppearance_Title);
            }
            else
            {
                mSets.setTextAppearance(mSets.getContext(), R.style.AppTheme_TextAppearance_Title);
            }
        }
        else
        {
            mAasanName.setVisibility(View.VISIBLE);
            mAasanName.setText(model.name);
            mSets.setText(String.format(Locale.getDefault(), "Number of sets: %d", model.numberOfSets));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                mSets.setTextAppearance(R.style.AppTheme_TextAppearance_Body1);
            }
            else
            {
                mSets.setTextAppearance(mSets.getContext(), R.style.AppTheme_TextAppearance_Body1);
            }
        }

        mItemView.setOnClickListener(itemClickListener);
    }
}