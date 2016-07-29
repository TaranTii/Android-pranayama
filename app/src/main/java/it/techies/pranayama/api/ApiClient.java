package it.techies.pranayama.api;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.techies.pranayama.BuildConfig;
import it.techies.pranayama.api.history.Aasan;
import it.techies.pranayama.api.history.HistoryRequest;
import it.techies.pranayama.api.login.ChangePasswordRequest;
import it.techies.pranayama.api.login.ForgotPasswordRequest;
import it.techies.pranayama.api.login.LoginRequest;
import it.techies.pranayama.api.login.LoginResponse;
import it.techies.pranayama.api.register.RegisterRequest;
import it.techies.pranayama.api.timing.AasanTime;
import it.techies.pranayama.api.token.ResetTokenRequest;
import it.techies.pranayama.api.token.ResetTokenResponse;
import it.techies.pranayama.api.user.UserProfile;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import timber.log.Timber;

/**
 * Created by jdtechies on 30/11/2015.
 */
public class ApiClient {

    public static ApiInterface getApiClient(final String email, final String token)
    {
        String API_BASE_URL;

        if (BuildConfig.DEBUG)
        {
            API_BASE_URL = "http://pranayama-seobudd-com-zea5ujo7re0f.runscope.net/api/v1/";
        }
        else
        {
            // jatinder
            // API_BASE_URL = "http://pranayama-seobudd-com-nmpui1qimgnd.runscope.net/api/v1/";

            // gurdeep
            // API_BASE_URL = "http://pranayama-seobudd-com-z5z6aw6f22su.runscope.net/api/v1/";

            // live
            API_BASE_URL = "http://pranayama.seobudd.com/api/v1/";
        }

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(
                    Interceptor.Chain chain) throws IOException
            {
                Request original = chain.request();

                Request request;

                // Customize the request, add custom headers
                if (email != null && token != null)
                {
                    request = original.newBuilder().header("Accept", "application/json")
                            .header("Authorization", "PR " + email + ":" + token)
                            .method(original.method(), original.body()).build();
                }
                else
                {
                    request = original.newBuilder().header("Accept", "application/json")
                            .method(original.method(), original.body()).build();
                }

                com.squareup.okhttp.Response response = chain.proceed(request);

                Timber.i("Response %s", response.toString());
                Timber.i("Response code: %d", response.code());

                // Customize or return the response
                return response;
            }
        });


        Retrofit retrofit = new Retrofit.Builder().baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).client(client).build();

        return retrofit.create(ApiInterface.class);
    }

    public interface ApiInterface {

        // 1
        @PUT("user/reset-token")
        Call<ResetTokenResponse> resetToken(
                @Body ResetTokenRequest resetToken
        );

        // 2
        @POST("user/login")
        Call<LoginResponse> login(
                @Body LoginRequest request
        );

        // 3
        @POST("user/forgot-password")
        Call<SuccessResponse> forgotPassword(
                @Body ForgotPasswordRequest request
        );

        // 4
        @POST("daily-routine/set-routine")
        Call<EmptyResponse> setDailyRoutine(
                @Body List<DailyRoutine> request
        );

        // 5
        @POST("daily-routine/list-database-contents")
        Call<List<Aasan>> getHistory(
                @Body HistoryRequest request
        );

        // 6
        @GET("pranayama/get-pranayama-timings")
        Call<ArrayList<AasanTime>> getAasanTiming();

        // 7
        @POST("pranayama/set-pranayama-timings")
        Call<EmptyResponse> setPranayamaTiming(
                @Body List<AasanTime> request
        );

        // 8
        @PUT("user/change-password/{userId}")
        Call<SuccessResponse> changePassword(
                @Body ChangePasswordRequest request,
                @Path("userId") int userId
        );

        // 9
        @GET("user/view/{userId}")
        Call<UserProfile> getUserProfile(
                @Path("userId") int userId
        );

        // 10
        @PUT("user/update/{userId}")
        Call<EmptyResponse> updateUserProfile(
                @Body UserProfile userProfile,
                @Path("userId") int userId
        );

        // 11
        @POST("user/signup")
        Call<EmptyResponse> signup(
                @Body RegisterRequest request
        );

        // 12
        @DELETE("user/logout/{userId}")
        Call<EmptyResponse> doSignOut(
                @Path("userId") int userId
        );
    }
}
