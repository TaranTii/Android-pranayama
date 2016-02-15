package it.techies.pranayama.api.timing;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Created by jagdeep on 15/02/16.
 */
public class Timings {

    private long hours, minutes, seconds = 0;

    /**
     * Single set duration in milliseconds.
     */
    private long singleSetDuration;

    public Timings(@NonNull String time)
    {
        // parse the aasan time string, i.e "00:00:30"
        String[] timeArray = time.split(":");

        // according to time format array length should be 3
        if (timeArray.length == 3)
        {
            try
            {
                hours = Long.valueOf(timeArray[0]);
                minutes = Long.valueOf(timeArray[1]);
                seconds = Long.valueOf(timeArray[2]);

                singleSetDuration = (hours * 3600 + minutes * 60 + seconds) * 1000;
            } catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Timings format should be 00:00:00");
            }
        }
        else
        {
            throw new IllegalArgumentException("Timings format should be 00:00:00");
        }
    }

    public long getHours()
    {
        return hours;
    }

    public long getMinutes()
    {
        return minutes;
    }

    public long getSeconds()
    {
        return seconds;
    }

    public long getTotalTimeInSeconds()
    {
        return (hours * 3600 + minutes * 60 + seconds);
    }

    public long getSingleSetDuration()
    {
        return singleSetDuration;
    }

    public void addSeconds(long seconds)
    {
        // TODO: 15/02/16 implement this
    }

    @Override
    public String toString()
    {
        return String.format(Locale.getDefault(), "%2d:%2d:%2d:", hours, minutes, seconds);
    }
}