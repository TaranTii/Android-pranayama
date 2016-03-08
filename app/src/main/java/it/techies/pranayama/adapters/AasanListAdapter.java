package it.techies.pranayama.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.techies.pranayama.R;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.api.timing.Timings;
import timber.log.Timber;

/**
 * Created by jdtechies on 11/12/2015.
 */
public class AasanListAdapter extends ArrayAdapter<AasanTime> {

    private static final int ITEM_TYPE_AASAN = 0;
    private static final int ITEM_TYPE_BREAK = 1;

    private Activity context;

    private ArrayList<AasanTime> aasanTimeList;

    public AasanListAdapter(Activity context, ArrayList<AasanTime> list)
    {
        super(context, R.layout.list_row_aasan_time, list);
        this.context = context;
        this.aasanTimeList = list;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position == 7)
        {
            return ITEM_TYPE_BREAK;
        }
        return ITEM_TYPE_AASAN;
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getCount()
    {
        return aasanTimeList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (getItemViewType(position) == ITEM_TYPE_AASAN)
        {
            convertView = bindAasanView(position, convertView, parent);
        }
        else if (getItemViewType(position) == ITEM_TYPE_BREAK)
        {
            convertView = bindBreakTimeView(position, convertView, parent);
        }

        return convertView;
    }

    @NonNull
    protected View bindAasanView(int position, View convertView, ViewGroup parent)
    {
        AasanViewHolder viewHolder;

        if (convertView == null)
        {
            viewHolder = new AasanViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_row_aasan_time, parent, false);

            viewHolder.aasanName = (TextView) convertView.findViewById(R.id.aasan_name_tv);
            viewHolder.aasanTime = (TextView) convertView.findViewById(R.id.time_tv);
            viewHolder.sets = (TextView) convertView.findViewById(R.id.set_tv);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (AasanViewHolder) convertView.getTag();
        }

        AasanTime aasanTime = aasanTimeList.get(position);

        viewHolder.aasanName.setText(aasanTime.getName());
        viewHolder.aasanTime.setText(String.format("Time: %s", aasanTime.getTime()));
        viewHolder.sets.setText(String.format(Locale.getDefault(), "Sets: %d", aasanTime.getSet()));

        return convertView;
    }

    @NonNull
    protected View bindBreakTimeView(int position, View convertView, ViewGroup parent)
    {
        BreakViewHolder viewHolder;

        if (convertView == null)
        {
            viewHolder = new BreakViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_row_break_time, parent, false);

            viewHolder.breakTime = (TextView) convertView.findViewById(R.id.break_time_tv);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (BreakViewHolder) convertView.getTag();
        }

        AasanTime aasanTime = aasanTimeList.get(position);

        Timings breakTimings = new Timings("00:00:00");
        breakTimings.addSeconds(aasanTime.getBreakTime());

        viewHolder.breakTime.setText(String.format("Time: %s", breakTimings.toString()));

        return convertView;
    }

    public List<AasanTime> getAasanList()
    {
        return aasanTimeList;
    }

    public void setBreakTime(long breakTime)
    {
        for (int i = 0; i < getCount(); i++)
        {
            aasanTimeList.get(i).setBreakTime(breakTime);
        }
        notifyDataSetChanged();
    }

    static class AasanViewHolder {
        public TextView sets, aasanName, aasanTime;
    }

    static class BreakViewHolder {
        public TextView breakTime;
    }
}
