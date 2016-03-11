package it.techies.pranayama;

import org.junit.Test;

import it.techies.pranayama.api.user.UserProfile;
import it.techies.pranayama.utils.Utils;
import timber.log.Timber;

import static org.junit.Assert.assertEquals;

/**
 * Created by jagdeep on 11/03/16.
 */
public class DateOfBirthTest {

    @Test()
    public void date_can_be_parsed()
    {
        String dob = "1990-12-01";

        String[] dateOfBirth = dob.split("-", 3);
        int year = Integer.valueOf(dateOfBirth[0]);
        int month = Integer.valueOf(dateOfBirth[1]);
        int day = Integer.valueOf(dateOfBirth[2]);

        assertEquals(year, 1990);
        assertEquals(month, 12);
        assertEquals(day, 1);
    }

    @Test()
    public void date_of_birth_is_valid()
    {
        String dob = "1990-12-01";
        boolean isValid = Utils.isDateOfBirthValid(dob);
        assertEquals(true, isValid);
    }

    @Test()
    public void date_of_birth_is_not_valid_1()
    {
        String dob = "1990-12-00";
        boolean isValid = Utils.isDateOfBirthValid(dob);
        assertEquals(false, isValid);
    }

    @Test()
    public void date_of_birth_is_not_valid_2()
    {
        String dob = "1990-12-32";
        boolean isValid = Utils.isDateOfBirthValid(dob);
        assertEquals(false, isValid);
    }

    @Test()
    public void date_of_birth_is_not_valid_3()
    {
        String dob = "1990-00-01";
        boolean isValid = Utils.isDateOfBirthValid(dob);
        assertEquals(false, isValid);
    }

    @Test()
    public void date_of_birth_is_not_valid_4()
    {
        String dob = "1990-13-01";
        boolean isValid = Utils.isDateOfBirthValid(dob);
        assertEquals(false, isValid);
    }

    @Test()
    public void date_of_birth_is_not_valid_5()
    {
        String dob = "0000-12-01";
        boolean isValid = Utils.isDateOfBirthValid(dob);
        assertEquals(false, isValid);
    }

    @Test()
    public void date_of_birth_is_not_valid_6()
    {
        String dob = "9999-13-01";
        boolean isValid = Utils.isDateOfBirthValid(dob);
        assertEquals(false, isValid);
    }

}
