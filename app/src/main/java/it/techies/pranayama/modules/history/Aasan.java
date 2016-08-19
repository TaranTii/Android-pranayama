package it.techies.pranayama.modules.history;

/**
 * Created by jagdeep on 16/08/16.
 */
public class Aasan {

    public String aasanKey;
    public int duration;
    public long timestamp;

    public Aasan()
    {
    }

    @Override
    public String toString()
    {
        return "Aasan{" +
                "aasanKey='" + aasanKey + '\'' +
                ", duration=" + duration +
                ", timestamp=" + timestamp +
                '}';
    }
}