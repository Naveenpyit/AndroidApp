package com.example.myapplication.network;

import com.example.myapplication.model.RefreshTokenRequest;
import com.example.myapplication.model.RefreshTokenResponse;
import com.example.myapplication.utils.TokenManager;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;



import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private TokenManager tokenManager;
    private ApiService apiService;

    public TokenAuthenticator(TokenManager tokenManager, ApiService apiService){
        this.tokenManager = tokenManager;
        this.apiService = apiService;
    }
    @Override
    public Request authenticate(Route route, Response response){

        String access = tokenManager.getAccess();

        try{

            Call<RefreshTokenResponse> call =
                    apiService.refreshToken(new RefreshTokenRequest(access));

            retrofit2.Response<RefreshTokenResponse> refreshResponse =
                    call.execute();

            if(refreshResponse.isSuccessful() && refreshResponse.body()!=null){

                String newToken =
                        refreshResponse.body().getJ_data().get(0).getJ_token();


                String newAccess =
                        refreshResponse.body().getJ_data().get(0).getJ_access();

                tokenManager.saveToken(newToken);
                tokenManager.saveAccess(newAccess);

                return response.request().newBuilder()
                        .header("Authorization","Bearer " + newToken)
                        .build();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
