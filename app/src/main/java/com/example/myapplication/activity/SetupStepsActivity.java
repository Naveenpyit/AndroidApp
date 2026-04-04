package com.example.myapplication.activity;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private ApiService     apiService;
    private ProgressDialog progressDialog;
    private String         mobile = "";

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

    // ─────────────────────────────────────────────
    // Init Views
    // ─────────────────────────────────────────────

    private void initializeViews() {
        btnContinue   = findViewById(R.id.btnContinue);
        tvSkip        = findViewById(R.id.tvSkip);
        business_doc  = findViewById(R.id.business_doc);
        continue_skip = findViewById(R.id.continue_skip);
        owner_details = findViewById(R.id.owner_details);
        business_det  = findViewById(R.id.business_det);
    }



    private void setupClickListeners() {


        tvSkip.setOnClickListener(v -> goToLoginPage());


        owner_details.setOnClickListener(v ->
                goToOwnerDetails("", mobile, ""));
        business_det.setOnClickListener(v  ->
                goToBusinessDetails("", mobile, "", null));
        business_doc.setOnClickListener(v  ->
                continue_skip.setVisibility(VISIBLE));


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

                        // n_step safe parse
                        int step = 0;
                        try {
                            step = Integer.parseInt(
                                    String.valueOf(body.getNStep()).trim());
                        } catch (Exception e) {
                            Log.e(TAG, "n_step parse error: " + e.getMessage());
                        }

                        Log.d(TAG, "n_status=" + body.getNStatus()
                                + " | n_step=" + step);

                        if (body.getNStatus() != 1) {
                            insertFirstTimeOwner();
                            return;
                        }

                        routeByStep(step, body);
                    }

                    @Override
                    public void onFailure(Call<RegisterDetailsResponse> call,
                                          Throwable t) {
                        hideLoader();
                        Log.e(TAG, "register-details failure: " + t.getMessage());
                        insertFirstTimeOwner();
                    }
                });
    }



    private void routeByStep(int step, RegisterDetailsResponse body) {
        RegisterDetailsResponse.JData data = body.getJData();

        int nOwner    = 0;
        int nBusiness = 0;
        int nAddress  = 0;

        if (data != null && data.getIsInformations() != null) {
            nOwner    = data.getIsInformations().getNOwner();
            nBusiness = data.getIsInformations().getNBusiness();
            nAddress  = data.getIsInformations().getNAddress();
        }

        Log.d(TAG, "is_informations → owner=" + nOwner
                + " | business=" + nBusiness
                + " | address=" + nAddress);

        if (nOwner == 0) {

            String name  = (data != null && data.getOwnerDetails() != null)
                    ? safe(data.getOwnerDetails().getCName())  : "";
            String email = (data != null && data.getOwnerDetails() != null)
                    ? safe(data.getOwnerDetails().getCEmail()) : "";

            Log.d(TAG, "→ OwnerDetails | name=" + name + " email=" + email);
            goToOwnerDetails(name, mobile, email);

        } else if (nBusiness == 0) {

            String name  = (data != null && data.getOwnerDetails() != null)
                    ? safe(data.getOwnerDetails().getCName())  : "";
            String email = (data != null && data.getOwnerDetails() != null)
                    ? safe(data.getOwnerDetails().getCEmail()) : "";

            Log.d(TAG, "→ BusinessDetails | name=" + name + " email=" + email);
            goToBusinessDetails(name, mobile, email, data);

        } else if (nAddress == 0) {

            Log.d(TAG, "→ DeliveryAddress");
            goToDeliveryAddress(data);

        } else {

            Log.d(TAG, "→ All steps done → LoginPage");
            goToLoginPage();
        }
    }



    private void insertFirstTimeOwner() {
        showLoader("Setting up...");

        RegisterInsertRequest req =
                RegisterInsertRequest.forOwner("", mobile, "");

        apiService.registerInsert(req)
                .enqueue(new Callback<RegisterInsertResponse>() {

                    @Override
                    public void onResponse(Call<RegisterInsertResponse> call,
                                           Response<RegisterInsertResponse> res) {
                        hideLoader();
                        Log.d(TAG, "insertFirstTimeOwner → status="
                                + (res.body() != null
                                ? res.body().getNStatus() : "null"));
                        goToOwnerDetails("", mobile, "");
                    }

                    @Override
                    public void onFailure(Call<RegisterInsertResponse> call,
                                          Throwable t) {
                        hideLoader();
                        Log.e(TAG, "insertFirstTimeOwner failure: "
                                + t.getMessage());
                        goToOwnerDetails("", mobile, "");
                    }
                });
    }



    private void goToOwnerDetails(String name, String mob, String email) {
        Intent i = new Intent(this, OwnerDetailsActivity.class);
        i.putExtra("mobile",      mob.isEmpty() ? mobile : mob);
        i.putExtra("fullName",    name);
        i.putExtra("email",       email);
        i.putExtra("isPrefilled", !name.isEmpty());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToBusinessDetails(String name, String mob, String email,
                                     RegisterDetailsResponse.JData data) {
        Intent i = new Intent(this, BusinessDetailsActivity.class);
        i.putExtra("fullName",     name);
        i.putExtra("mobileNumber", mob.isEmpty() ? mobile : mob);
        i.putExtra("email",        email);

        if (data != null && data.getBusinessDetails() != null) {
            i.putExtra("prefillBType",
                    safe(data.getBusinessDetails().getNType()));
            i.putExtra("prefillVerifyType",
                    safe(data.getBusinessDetails().getNVerifyType()));
            i.putExtra("prefillPan",
                    safe(data.getBusinessDetails().getCPan()));
            i.putExtra("prefillGst",
                    safe(data.getBusinessDetails().getCGst()));
        }

        startActivity(i);
        finish();
    }

    private void goToDeliveryAddress(RegisterDetailsResponse.JData data) {
        Intent i = new Intent(this, DeliveryAddressActivity.class);

        if (data != null && data.getOwnerDetails() != null) {
            i.putExtra("fullName",
                    safe(data.getOwnerDetails().getCName()));
            i.putExtra("mobileNumber",
                    safe(data.getOwnerDetails().getNMobile()));
            i.putExtra("email",
                    safe(data.getOwnerDetails().getCEmail()));
        }

        if (data != null && data.getAddressDetails() != null) {
            i.putExtra("prefillStateId",
                    safe(data.getAddressDetails().getNState()));
            i.putExtra("prefillCityId",
                    safe(data.getAddressDetails().getNCity()));
            i.putExtra("prefillAddress",
                    safe(data.getAddressDetails().getCAddress()));
            i.putExtra("prefillPin",
                    safe(data.getAddressDetails().getNPincode()));
            i.putExtra("prefillLat",
                    safe(data.getAddressDetails().getCLatitude()));
            i.putExtra("prefillLng",
                    safe(data.getAddressDetails().getCLongitude()));
            i.putExtra("prefillAddrType", "1");
        }

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToLoginPage() {
        Intent i = new Intent(this, LoginPage.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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



    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(
                            getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }


    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}