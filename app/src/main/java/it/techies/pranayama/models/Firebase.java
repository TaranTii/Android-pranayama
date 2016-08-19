package it.techies.pranayama.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.util.Locale;

/**
 * Created by jagdeep on 17/08/16.
 */
public class Firebase {

    public static class User {

        @Nullable
        public String displayName;

        @NonNull
        public boolean isFirstSetupCompleted;

        @Nullable
        public String email;

        @Nullable
        public String photoUrl;

        @NonNull
        public String uid;

        @Nullable
        public DateOfBirth dateOfBirth;

        @Nullable
        public String location;

        @Nullable
        public String phone;

        public User() {}

        public static class DateOfBirth {

            @Exclude
            public static final String KEY = "dateOfBirth";

            public Integer year;

            public Integer month;

            public Integer day;

            @Exclude
            public boolean isEmpty()
            {
                return year == null || month == null || day == null;
            }

            public DateOfBirth() {}

            public DateOfBirth(Integer year, Integer month, Integer day)
            {
                this.year = year;
                this.month = month;
                this.day = day;
            }

            @Override
            public String toString()
            {
                return String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        year,
                        month,
                        day
                );
            }
        }
    }


}
