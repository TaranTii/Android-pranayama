package it.techies.pranayama;

import org.junit.Test;

import it.techies.pranayama.api.timing.Timings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Timing class unit tests.
 * <p/>
 * Created by jagdeep on 18/02/16.
 */
public class TimingsTest {

    @Test(expected = IllegalArgumentException.class)
    public void check_format_isNotCorrect_test1()
    {
        new Timings("00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void check_format_isNotCorrect_test2()
    {
        new Timings("00:00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void check_format_isNotCorrect_test3()
    {
        new Timings("00:00:00:00");
    }

    @Test()
    public void format_isCorrect()
    {
        Timings timings = new Timings("00:00:00");
        assertNotNull(timings);
    }

    @Test()
    public void check_getters_areCorrect()
    {
        Timings timings = new Timings("00:00:00");

        assertEquals(0, timings.getHours());
        assertEquals(0, timings.getHours());
        assertEquals(0, timings.getHours());
    }

    @Test()
    public void check_hour_isCorrect()
    {
        Timings timings = new Timings("12:00:00");
        assertEquals(12, timings.getHours());
    }

    @Test()
    public void check_minute_isCorrect()
    {
        Timings timings = new Timings("00:12:00");
        assertEquals(12, timings.getMinutes());
    }

    @Test()
    public void check_second_isCorrect()
    {
        Timings timings = new Timings("00:00:12");
        assertEquals(12, timings.getSeconds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void minutes_can_not_be_greater_than_59()
    {
        new Timings("00:60:00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void seconds_can_not_be_greater_than_59()
    {
        new Timings("00:00:60");
    }

    @Test()
    public void hours_can_be_greater_than_60()
    {
        Timings timings = new Timings("89:00:00");
        assertNotNull(timings);
    }

    @Test()
    public void can_add_one_second()
    {
        Timings timings = new Timings("00:00:00");

        assertEquals(0, timings.getSeconds());

        timings.addSeconds(1);

        assertEquals(1, timings.getSeconds());
    }

    @Test()
    public void can_add_59_seconds()
    {
        Timings timings = new Timings("00:00:00");

        assertEquals(0, timings.getSeconds());

        timings.addSeconds(59);

        assertEquals(59, timings.getSeconds());
    }

    @Test()
    public void can_add_60_seconds()
    {
        Timings timings = new Timings("00:00:00");

        assertEquals(0, timings.getSeconds());
        assertEquals(0, timings.getMinutes());

        timings.addSeconds(60);

        assertEquals(0, timings.getSeconds());
        assertEquals(1, timings.getMinutes());
    }

    @Test()
    public void can_add_3600_seconds()
    {
        Timings timings = new Timings("00:00:00");

        assertEquals(0, timings.getSeconds());
        assertEquals(0, timings.getMinutes());
        assertEquals(0, timings.getHours());

        timings.addSeconds(3600);

        assertEquals(0, timings.getSeconds());
        assertEquals(0, timings.getMinutes());
        assertEquals(1, timings.getHours());
    }

    @Test()
    public void can_add_3661_seconds()
    {
        Timings timings = new Timings("00:00:00");

        assertEquals(0, timings.getSeconds());
        assertEquals(0, timings.getMinutes());
        assertEquals(0, timings.getHours());

        timings.addSeconds(3661);

        assertEquals(1, timings.getSeconds());
        assertEquals(1, timings.getMinutes());
        assertEquals(1, timings.getHours());
    }

    @Test(expected = IllegalArgumentException.class)
    public void can_not_remove_one_second()
    {
        Timings timings = new Timings("00:00:00");

        assertEquals(0, timings.getSeconds());

        timings.addSeconds(-1);
    }

    @Test()
    public void check_single_set_duration_isCorrect()
    {
        Timings timings = new Timings("01:01:30");
        assertEquals(3690000, timings.getSingleSetDuration());
    }

    @Test()
    public void check_total_time_in_seconds_isCorrect()
    {
        Timings timings = new Timings("01:01:30");
        assertEquals(3690, timings.getTotalTimeInSeconds());
    }

    @Test()
    public void check_string_format_isCorrect()
    {
        String time = "01:01:12";
        Timings timings = new Timings(time);
        assertEquals(time, timings.toString());
    }

}
