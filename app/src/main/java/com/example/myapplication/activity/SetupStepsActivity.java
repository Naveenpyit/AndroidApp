package com.example.myapplication.activity;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.RegisterDetailsRequest;
import com.example.myapplication.model.RegisterDetailsResponse;
import com.example.myapplication.model.RegisterInsertRequest;
import com.example.myapplication.model.RegisterInsertResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetupStepsActivity extends AppCompatActivity {

    private static final String TAG = "SetupStepsActivity";

    private MaterialButton btnContinue;
    private TextView       tvSkip;
    private LinearLayout   business_doc, continue_skip, owner_details, business_det;

    private ApiService      apiService;
    private ProgressDialog  progressDialog;
    private String          mobile = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_steps);

        setupStatusBar();
        apiService = RetrofitClient.getClient(this);
        setupLoader();


        mobile = getIntent().getStringExtra("mobile");
        if (mobile == null) mobile = "";

        initializeViews();
        setupClickListeners();


        fetchRegisterDetails();
    }


    private void initializeViews() {
        btnContinue   = findViewById(R.id.btnContinue);
        tvSkip        = findViewById(R.id.tvSkip);
        business_doc  = findViewById(R.id.business_doc);
        continue_skip = findViewById(R.id.continue_skip);
        owner_details = findViewById(R.id.owner_details);
        business_det  = findViewById(R.id.business_det);
    }

    private void setupClickListeners() {

        // Skip → MainActivity
        tvSkip.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Manual navigate buttons (backup)
        owner_details.setOnClickListener(v -> goToOwnerDetails("", mobile, ""));
        business_det.setOnClickListener(v  -> goToBusinessDetails("", mobile, "", null));
        business_doc.setOnClickListener(v  -> continue_skip.setVisibility(VISIBLE));

        // Continue → re-fetch and route
        btnContinue.setOnClickListener(v -> fetchRegisterDetails());
    }



    private void fetchRegisterDetails() {
        showLoader("Loading...");

        Log.d(TAG, "fetchRegisterDetails → mobile=" + mobile);

        apiService.getRegisterDetails(new RegisterDetailsRequest(mobile))
                .enqueue(new Callback<RegisterDetailsResponse>() {

                    @Override
                    public void onResponse(Call<RegisterDetailsResponse> call,
                                           Response<RegisterDetailsResponse> res) {
                        hideLoader();

                        if (!res.isSuccessful() || res.body() == null) {
                            Log.e(TAG, "API failed → first time user");
                            insertFirstTimeOwner();
                            return;
                        }

                        RegisterDetailsResponse body = res.body();
                        Log.d(TAG, "n_status=" + body.getNStatus()
                                + " | n_step=" + body.getNStep());

                        if (body.getNStatus() != 1) {
                            // User இல்லை → first time
                            insertFirstTimeOwner();
                            return;
                        }

                        routeByStep(body.getNStep(), body);
                    }

                    @Override
                    public void onFailure(Call<RegisterDetailsResponse> call, Throwable t) {
                        hideLoader();
                        Log.e(TAG, "register-details failure: " + t.getMessage());
                        // Network error → first time treat பண்ணு
                        insertFirstTimeOwner();
                    }
                });
    }


    private void routeByStep(int step, RegisterDetailsResponse body) {

        RegisterDetailsResponse.JData data = body.getJData();

        Log.d(TAG, "routeByStep → step=" + step);

        switch (step) {

            case 1:
                // Owner details இல்லை
                goToOwnerDetails("", mobile, "");
                break;

            case 2:
                // Owner done, business pending
                String name2  = data != null && data.getOwnerDetails() != null
                        ? data.getOwnerDetails().getCName()  : "";
                String email2 = data != null && data.getOwnerDetails() != null
                        ? data.getOwnerDetails().getCEmail() : "";
                goToBusinessDetails(name2, mobile, email2, data);
                break;

            case 3:
                // Business done, address pending
                goToDeliveryAddress(data);
                break;

            default:
                // All done → Main
                goToMain();
                break;
        }
    }



    private void insertFirstTimeOwner() {
        showLoader("Setting up...");

        // ✅ First time — mobile மட்டும் pass பண்ணு
        RegisterInsertRequest req = RegisterInsertRequest.forOwner("", mobile, "");

        apiService.registerInsert(req).enqueue(new Callback<RegisterInsertResponse>() {

            @Override
            public void onResponse(Call<RegisterInsertResponse> call,
                                   Response<RegisterInsertResponse> res) {
                hideLoader();

                Log.d(TAG, "insertFirstTimeOwner → status="
                        + (res.body() != null ? res.body().getNStatus() : "null"));

                // Success or fail → OwnerDetails போ
                goToOwnerDetails("", mobile, "");
            }

            @Override
            public void onFailure(Call<RegisterInsertResponse> call, Throwable t) {
                hideLoader();
                Log.e(TAG, "insertFirstTimeOwner failure: " + t.getMessage());
                goToOwnerDetails("", mobile, "");
            }
        });
    }



    private void goToOwnerDetails(String name, String mob, String email) {
        Intent i = new Intent(this, OwnerDetailsActivity.class);
        i.putExtra("mobile",   mob.isEmpty() ? mobile : mob);
        i.putExtra("fullName", name);
        i.putExtra("email",    email);
        startActivity(i);
        finish();
    }

    private void goToBusinessDetails(String name, String mob, String email,
                                     RegisterDetailsResponse.JData data) {
        Intent i = new Intent(this, BusinessDetailsActivity.class);
        i.putExtra("fullName",     name);
        i.putExtra("mobileNumber", mob.isEmpty() ? mobile : mob);
        i.putExtra("email",        email);

        // ✅ Pre-fill business data (returning user)
        if (data != null && data.getBusinessDetails() != null) {
            i.putExtra("prefillBType",      data.getBusinessDetails().getNType());
            i.putExtra("prefillVerifyType", data.getBusinessDetails().getNVerifyType());
            i.putExtra("prefillPan",        data.getBusinessDetails().getCPan());
            i.putExtra("prefillGst",        data.getBusinessDetails().getCGst());
        }
        startActivity(i);
        finish();
    }

    private void goToDeliveryAddress(RegisterDetailsResponse.JData data) {
        Intent i = new Intent(this, DeliveryAddressActivity.class);
        if (data != null && data.getOwnerDetails() != null) {
            i.putExtra("fullName",     data.getOwnerDetails().getCName());
            i.putExtra("mobileNumber", data.getOwnerDetails().getNMobile());
            i.putExtra("email",        data.getOwnerDetails().getCEmail());
        }
        if (data != null && data.getAddressDetails() != null) {
            i.putExtra("prefillState",   data.getAddressDetails().getNState());
            i.putExtra("prefillCity",    data.getAddressDetails().getNCity());
            i.putExtra("prefillAddress", data.getAddressDetails().getCAddress());
            i.putExtra("prefillPin",     data.getAddressDetails().getNPincode());
            i.putExtra("prefillLat",     data.getAddressDetails().getCLatitude());
            i.putExtra("prefillLng",     data.getAddressDetails().getCLongitude());
        }
        startActivity(i);
        finish();
    }

    private void goToMain() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit().putString("last_screen", "Main").apply();

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    // ─────────────────────────────────────────────
    // Loader
    // ─────────────────────────────────────────────

    private void setupLoader() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void showLoader(String msg) {
        progressDialog.setMessage(msg);
        if (!progressDialog.isShowing()) progressDialog.show();
    }

    private void hideLoader() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    // ─────────────────────────────────────────────
    // Status Bar
    // ─────────────────────────────────────────────

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }
}