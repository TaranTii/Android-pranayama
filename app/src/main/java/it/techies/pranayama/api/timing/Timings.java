package it.techies.pranayama.api.timing;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * Wrapper class for conversion and addition of aasan timing for daily routine.
 * <p/>
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
                if (minutes > 59)
                {
                    throw new IllegalArgumentException("Minutes cannot be greater than 59");
                }

                seconds = Long.valueOf(timeArray[2]);
                if (seconds > 59)
                {
                    throw new IllegalArgumentException("Seconds cannot be greater than 59");
                }

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

    /**
     * Adds the number of seconds in the current timing.
     *
     * @param sec Seconds
     */
    public void addSeconds(long sec)
    {
        if (sec < 0)
        {
            throw new IllegalArgumentException("sec should be a positive number");
        }

        long h, m, s;

        h = sec / 3600;
        m = (sec % 3600) / 60;
        s = (sec % 3600) % 60;

        if (s + seconds > 59)
        {
            seconds = (s + seconds) % 60;
            addOneMinute();
        }
        else
        {
            seconds += s;
        }

        if (minutes + m > 59)
        {
            minutes = (m + minutes) % 60;
            addOneHour();
        }
        else
        {
            minutes += m;
        }

        hours += h;
    }

    /**
     * Adds one minute to the current timing.
     */
    private void addOneMinute()
    {
        if (minutes + 1 > 59)
        {
            minutes = 0;
            addOneHour();
        }
        else
        {
            minutes++;
        }
    }

    /**
     * Adds one hour to the current timing.
     */
    private void addOneHour()
    {
        hours++;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}