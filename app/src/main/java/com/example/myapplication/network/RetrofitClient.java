package com.example.myapplication.network;

import android.content.Context;

import com.example.myapplication.utils.TokenManager;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static ApiService getClient(Context context) {

        if (retrofit != null) {
            return retrofit.create(ApiService.class);
        }

        TokenManager tokenManager = new TokenManager(context);

        // Logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // TLS 1.2 fix (important for Juspay + older devices)
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();

        // Temporary retrofit for refresh token API
        Retrofit tempRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.tomhiddleb2b.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService tempApi = tempRetrofit.create(ApiService.class);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager))
                .authenticator(new TokenAuthenticator(tokenManager, tempApi))
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .connectionSpecs(Arrays.asList(spec, ConnectionSpec.CLEARTEXT))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.tomhiddleb2b.com/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }
}