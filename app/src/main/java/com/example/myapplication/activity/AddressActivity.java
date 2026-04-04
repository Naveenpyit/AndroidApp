package com.example.myapplication.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.AddressAdapter;
import com.example.myapplication.model.AddressData;
import com.example.myapplication.model.AddressResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AddressAdapter adapter;
    private List<AddressData> addressList = new ArrayList<>();
    private ApiService apiService;
    private TokenManager tokenManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        initViews();
        initApi();
        getAddressList();
    }

    // ✅ Initialize Views
    private void initViews() {
        recyclerView = findViewById(R.id.addressRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // ✅ Initialize API (FIX HERE 🔥)
    private void initApi() {
        apiService = RetrofitClient.getClient(this);
    }

    // ✅ API Call
    private void getAddressList() {
        String userId = "28";

        if (userId == null || userId.isEmpty()) {
            Log.e("USER_ID", "User ID is null");
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("n_user",userId);

        Call<AddressResponse> call = apiService.getAddress(body);

        call.enqueue(new Callback<AddressResponse>() {
            @Override
            public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    AddressResponse res = response.body();

                    if (res.getN_status() == 1 && res.getJ_data() != null) {

                        addressList.clear();
                        addressList.addAll(res.getJ_data());

                        if (adapter == null) {
                            adapter = new AddressAdapter(AddressActivity.this, addressList);
                            recyclerView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }

                    } else {
                        Log.e("API", "No Data Found");
                    }

                } else {
                    Log.e("API", "Response Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AddressResponse> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }
}