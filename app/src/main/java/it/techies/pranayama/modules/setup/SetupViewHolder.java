package it.techies.pranayama.modules.setup;

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

    public void bindToPost(FirebaseSchedule model, View.OnClickListener starClickListener)
    {
        mAasanName.setText(model.name);
        mAasanTime.setText(String.format("Time: %s", model.duration));
        mSets.setText(String.format(Locale.getDefault(), "Sets: %d", model.sets));

        mItemView.setOnClickListener(starClickListener);
    }
}