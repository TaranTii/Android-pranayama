package it.techies.pranayama.adapters;

import android.app.Activity;
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

/**
 * Created by jdtechies on 11/12/2015.
 */
public class AasanListAdapter extends ArrayAdapter<AasanTime>
{
    private Activity context;

    private ArrayList<AasanTime> aasanTimeList;

    public AasanListAdapter(Activity context, ArrayList<AasanTime> list)
    {
        super(context, R.layout.list_row_aasan_time, list);
        this.context = context;
        this.aasanTimeList = list;
    }

    @Override
    public int getCount()
    {
        return aasanTimeList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_row_aasan_time, parent, false);

            viewHolder.aasanName = (TextView) convertView.findViewById(R.id.aasan_name_tv);
            viewHolder.aasanTime = (TextView) convertView.findViewById(R.id.time_tv);
            viewHolder.sets = (TextView) convertView.findViewById(R.id.set_tv);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AasanTime aasanTime = aasanTimeList.get(position);

        viewHolder.aasanName.setText(aasanTime.getName());
        viewHolder.aasanTime.setText(String.format("Time: %s", aasanTime.getTime()));
        viewHolder.sets.setText(String.format(Locale.getDefault(), "Sets: %d", aasanTime.getSet()));

        return convertView;
    }

    public List<AasanTime> getAasanList()
    {
        return aasanTimeList;
    }

    static class ViewHolder
    {
        public TextView sets;

        public TextView aasanName;

        public TextView aasanTime;
    }
}
