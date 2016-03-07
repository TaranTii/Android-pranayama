package it.techies.pranayama.utils;

/**
 * Created by jagdeep on 07/03/16.
 */
public class Timezone {

    private String name;
    private String value;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
