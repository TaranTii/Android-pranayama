package it.techies.pranayama;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.techies.pranayama.activities.LoginActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Login activity tests.
 * <p/>
 * Created by jagdeep on 18/02/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SignInTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @Test
    public void fillEmail_sameActivity()
    {
        onView(withId(R.id.email))
                .check(matches(withText("jagdeep@yopmail.com")));

        onView(withId(R.id.password))
                .check(matches(withText("Singh@1")));

    }

}
