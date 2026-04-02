package com.example.myapplication.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.*;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.*;

import retrofit2.*;

public class DeliveryAddressActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQ = 1001;

    private Spinner spinnerState, spinnerCity;
    private EditText etAddressLine, etPinCode;
    private TextView tvCurrentLocation;
    private MaterialButton btnUseLocation, btnContinue;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private double lat = 0.0, lng = 0.0;

    private ApiService apiService;
    private ProgressDialog dialog;

    private List<StateListResponse.StateData> stateList = new ArrayList<>();
    private List<CityListResponse.CityData> cityList = new ArrayList<>();

    private String selectedStateId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        initViews();

        apiService = RetrofitClient.getClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupLoader();
        setupMap();
        setupClick();

        fetchStateList(); // API CALL
    }

    private void initViews() {
        spinnerState = findViewById(R.id.spinner_state);
        spinnerCity = findViewById(R.id.spinner_city);
        etAddressLine = findViewById(R.id.et_address_line);
        etPinCode = findViewById(R.id.et_pin_code);
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        btnUseLocation = findViewById(R.id.btn_use_location);
        btnContinue = findViewById(R.id.btn_continue);
    }

    // ================= MAP =================

    private void setupMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map_fragment);

        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        LatLng india = new LatLng(20.5937, 78.9629);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(india, 5));

        googleMap.setOnMapClickListener(latLng -> {
            lat = latLng.latitude;
            lng = latLng.longitude;

            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));

            reverseGeocode(lat, lng);
        });
    }

    // ================= LOCATION =================

    private void setupClick() {

        btnUseLocation.setOnClickListener(v -> requestLocation());

        btnContinue.setOnClickListener(v -> saveData());
    }

    private void requestLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQ);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();

                LatLng latLng = new LatLng(lat, lng);

                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                reverseGeocode(lat, lng);

                Toast.makeText(this, "Location Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= GEOCODER =================

    private void reverseGeocode(double lat, double lng) {

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(lat, lng, 1);

                if (list != null && !list.isEmpty()) {

                    Address a = list.get(0);

                    String address = a.getAddressLine(0);
                    String city = a.getLocality();
                    String state = a.getAdminArea();
                    String pin = a.getPostalCode();

                    runOnUiThread(() -> {

                        tvCurrentLocation.setText(address);
                        etAddressLine.setText(address);

                        if (pin != null) etPinCode.setText(pin);

                        setSpinnerByText(spinnerState, state);
                        setSpinnerByText(spinnerCity, city);
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ================= STATE API =================

    // DeliveryAddressActivity.java — fetchStateList() fix

    private void fetchStateList() {
        dialog.show(); // loader காட்டு

        apiService.getStateList().enqueue(new Callback<StateListResponse>() {
            @Override
            public void onResponse(Call<StateListResponse> call, Response<StateListResponse> response) {
                dialog.dismiss();

                if (response.isSuccessful() && response.body() != null
                        && response.body().getNStatus() == 1) { // ← status check சேர்

                    stateList = response.body().getJData();

                    List<String> names = new ArrayList<>();
                    names.add("Select State");
                    for (StateListResponse.StateData s : stateList) {
                        names.add(s.getCStateName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            DeliveryAddressActivity.this,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerState.setAdapter(adapter);

                    spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            if (pos > 0) {
                                selectedStateId = stateList.get(pos - 1).getNId();
                                fetchCityList(selectedStateId);
                            }
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });

                } else {
                    // Response body null or status != 1
                    Toast.makeText(DeliveryAddressActivity.this,
                            "State list empty / API error", Toast.LENGTH_LONG).show();
                    Log.e("API", "State Response: " + response.code() + " body: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<StateListResponse> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(DeliveryAddressActivity.this,
                        "State API Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API", "State Fail", t); // ← Logcat-ல் பார்க்கலாம்
            }
        });
    }

    // ================= CITY API =================

    private void fetchCityList(String stateId) {

        apiService.getCityList(new CityListRequest(stateId))
                .enqueue(new Callback<CityListResponse>() {
                    @Override
                    public void onResponse(Call<CityListResponse> call, Response<CityListResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            cityList = response.body().getJData();

                            List<String> names = new ArrayList<>();
                            names.add("Select City");

                            for (CityListResponse.CityData c : cityList) {
                                names.add(c.getCCityName());
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    DeliveryAddressActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    names
                            );

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCity.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<CityListResponse> call, Throwable t) {
                        Toast.makeText(DeliveryAddressActivity.this, "City API Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= SAVE =================

    private void saveData() {

        String address = etAddressLine.getText().toString();
        String pin = etPinCode.getText().toString();

        if (lat == 0.0 || lng == 0.0) {
            Toast.makeText(this, "Select Location", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,
                "Lat: " + lat + "\nLng: " + lng,
                Toast.LENGTH_LONG).show();

        // 👉 இங்க API call போடு (register insert)
    }

    // ================= HELPERS =================

    private void setSpinnerByText(Spinner spinner, String text) {

        if (text == null) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;

        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(text)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupLoader() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
    }
}