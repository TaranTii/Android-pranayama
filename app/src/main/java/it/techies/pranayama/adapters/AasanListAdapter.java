package it.techies.pranayama.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import it.techies.pranayama.R;
import it.techies.pranayama.api.timing.AasanTime;

/**
 * Created by jdtechies on 11/12/2015.
 */
public class AasanListAdapter extends ListAdapter
{
    private Activity context;

    private List<AasanTime> aasanTimeList;

    public AasanListAdapter(Activity context, List<AasanTime> aasanTimeList)
    {
        this.aasanTimeList = aasanTimeList;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return aasanTimeList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return aasanTimeList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;

        // reuse views
        if (rowView == null)
        {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_row_aasan_time, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.sets = (TextView) rowView.findViewById(R.id.set_tv);
            viewHolder.aasanName = (TextView) rowView.findViewById(R.id.aasan_name_tv);
            viewHolder.aasanTime = (TextView) rowView.findViewById(R.id.time_tv);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        AasanTime aasanTime = aasanTimeList.get(position);

        holder.aasanName.setText(aasanTime.getName());
        holder.aasanName.setText(aasanTime.getTime());
        holder.sets.setText(aasanTime.getSet());

        return rowView;
    }

    static class ViewHolder
    {
        public TextView sets;
        public TextView aasanName;
        public TextView aasanTime;
    }
}
