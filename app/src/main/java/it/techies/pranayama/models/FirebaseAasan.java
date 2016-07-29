package it.techies.pranayama.models;

/**
 * Created by jagdeep on 27/07/16.
 */
public class FirebaseAasan {

    public String benefits;
    public String name;
    public Integer order;

    public FirebaseAasan()
    {
    }

    @Override
    public String toString()
    {
        return "FirebaseAasan{" +
                "benefits='" + benefits + '\'' +
                ", name='" + name + '\'' +
                ", order=" + order +
                '}';
    }
}
