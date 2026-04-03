package com.example.myapplication.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    private static final String TAG = "DeliveryAddressActivity";
    private static final int LOCATION_PERMISSION_REQ = 1001;

    private Spinner        spinnerState, spinnerCity, spinnerCountry, spinnerAddressType;
    private EditText       etAddressLine, etPinCode;
    private TextView       tvCurrentLocation;
    private MaterialButton btnUseLocation, btnContinue;

    private GoogleMap                   googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private double lat = 0.0, lng = 0.0;

    private ApiService     apiService;
    private ProgressDialog dialog;

    private List<StateListResponse.StateData> stateList = new ArrayList<>();
    private List<CityListResponse.CityData>   cityList  = new ArrayList<>();

    private String selectedStateId = "";
    private String selectedCityId  = "";
    private String pendingCity     = null;

    // prefill
    private String mobileNumber    = "";
    private String prefillStateId  = "";
    private String prefillCityId   = "";
    private String prefillAddrType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        initViews();
        apiService          = RetrofitClient.getClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupStatusBar();
        setupLoader();
        setupMap();
        setupClick();
        setupAddressTypeSpinner();
        setupCountrySpinner();

        // ── Intent extras ─────────────────────────────────────────────────────
        mobileNumber    = safe(getIntent().getStringExtra("mobileNumber"));
        prefillStateId  = safe(getIntent().getStringExtra("prefillStateId"));
        prefillCityId   = safe(getIntent().getStringExtra("prefillCityId"));
        prefillAddrType = safe(getIntent().getStringExtra("prefillAddrType"));

        String prefillAddress = safe(getIntent().getStringExtra("prefillAddress"));
        String prefillPin     = safe(getIntent().getStringExtra("prefillPin"));
        String prefillLat     = safe(getIntent().getStringExtra("prefillLat"));
        String prefillLng     = safe(getIntent().getStringExtra("prefillLng"));

        if (!prefillAddress.isEmpty()) {
            etAddressLine.setText(prefillAddress);
            tvCurrentLocation.setText(prefillAddress);
        }
        if (!prefillPin.isEmpty()) etPinCode.setText(prefillPin);

        if (!prefillLat.isEmpty() && !prefillLng.isEmpty()) {
            try {
                lat = Double.parseDouble(prefillLat);
                lng = Double.parseDouble(prefillLng);
            } catch (NumberFormatException ignored) {}
        }

        // Prefill address type (1=Home, 2=Office, 3=Other → index 0,1,2)
        if (!prefillAddrType.isEmpty()) {
            try {
                int idx = Integer.parseInt(prefillAddrType) - 1;
                if (idx >= 0 && idx <= 2)
                    spinnerAddressType.setSelection(idx);
            } catch (NumberFormatException ignored) {}
        }

        fetchStateList();
    }

    // ================= STATUS BAR =================

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        }
    }

    // ================= INIT =================

    private void initViews() {
        spinnerState       = findViewById(R.id.spinner_state);
        spinnerCity        = findViewById(R.id.spinner_city);
        spinnerCountry     = findViewById(R.id.spinner_country);
        spinnerAddressType = findViewById(R.id.spinner_address_type);
        etAddressLine      = findViewById(R.id.et_address_line);
        etPinCode          = findViewById(R.id.et_pin_code);
        tvCurrentLocation  = findViewById(R.id.tv_current_location);
        btnUseLocation     = findViewById(R.id.btn_use_location);
        btnContinue        = findViewById(R.id.btn_continue);
    }

    private void setupLoader() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
    }

    private void setupClick() {
        btnUseLocation.setOnClickListener(v -> requestLocation());
        btnContinue.setOnClickListener(v -> saveData());
    }

    // ================= ADDRESS TYPE =================

    private void setupAddressTypeSpinner() {
        List<String> types = Arrays.asList("Home", "Office", "Other");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, types);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerAddressType.setAdapter(adapter);
        spinnerAddressType.setSelection(0);
    }

    // ================= COUNTRY =================

    private void setupCountrySpinner() {
        List<String> countries = new ArrayList<>();
        for (String iso : Locale.getISOCountries()) {
            countries.add(new Locale("", iso).getDisplayCountry());
        }
        Collections.sort(countries);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, countries);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCountry.setAdapter(adapter);
        setSpinnerByText(spinnerCountry, "India");
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
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Show prefill marker if lat/lng available
        if (lat != 0.0 && lng != 0.0) {
            LatLng latLng = new LatLng(lat, lng);
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(20.5937, 78.9629), 5));
            // Auto-fetch location if permission granted
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }
        }

        googleMap.setOnMapClickListener(latLng -> {
            lat = latLng.latitude;
            lng = latLng.longitude;
            googleMap.clear();
            try { googleMap.setMyLocationEnabled(true); } catch (Exception ignored) {}
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            reverseGeocode(lat, lng);
        });
    }

    // ================= LOCATION =================

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQ);
            return;
        }

        try { googleMap.setMyLocationEnabled(true); } catch (Exception ignored) {}
        tvCurrentLocation.setText("Fetching location...");

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                handleLocation(location);
            } else {
                // Last location null → request fresh
                LocationRequest req = new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY, 0)
                        .setMaxUpdates(1).build();

                LocationCallback cb = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult result) {
                        if (!result.getLocations().isEmpty()) {
                            handleLocation(result.getLocations().get(0));
                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                };

                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(
                            req, cb, getMainLooper());
                }
            }
        });
    }

    private void handleLocation(android.location.Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        LatLng latLng = new LatLng(lat, lng);
        googleMap.clear();
        try { googleMap.setMyLocationEnabled(true); } catch (Exception ignored) {}
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        reverseGeocode(lat, lng);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQ
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
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
                    String city    = a.getLocality();
                    String state   = a.getAdminArea();
                    String pin     = a.getPostalCode();
                    String country = a.getCountryName();

                    runOnUiThread(() -> {
                        tvCurrentLocation.setText("Location fetched successfully");
                        new Handler().postDelayed(() ->
                                tvCurrentLocation.setText(address), 1000);

                        etAddressLine.setText(address);
                        if (pin != null) etPinCode.setText(pin);
                        if (country != null) setSpinnerByText(spinnerCountry, country);
                        setSpinnerByText(spinnerState, state);
                        pendingCity = city;
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ================= STATE API =================

    private void fetchStateList() {
        dialog.show();

        apiService.getStateList().enqueue(new Callback<StateListResponse>() {
            @Override
            public void onResponse(Call<StateListResponse> call,
                                   Response<StateListResponse> response) {
                dialog.dismiss();

                if (response.isSuccessful() && response.body() != null
                        && response.body().getNStatus() == 1) {

                    stateList = response.body().getJData();

                    // Remove nulls
                    Iterator<StateListResponse.StateData> it = stateList.iterator();
                    while (it.hasNext()) {
                        if (it.next().getCStateName() == null) it.remove();
                    }

                    ArrayAdapter<StateListResponse.StateData> adapter =
                            new ArrayAdapter<>(DeliveryAddressActivity.this,
                                    R.layout.spinner_item, stateList);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerState.setAdapter(adapter);

                    // ✅ Prefill state by ID
                    if (!prefillStateId.isEmpty()) {
                        for (int i = 0; i < stateList.size(); i++) {
                            if (stateList.get(i).getNId().equals(prefillStateId)) {
                                spinnerState.setSelection(i);
                                selectedStateId = prefillStateId;
                                break;
                            }
                        }
                    }

                    spinnerState.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent,
                                                           android.view.View view,
                                                           int pos, long id) {
                                    selectedStateId = stateList.get(pos).getNId();
                                    // Pass prefillCityId first time only
                                    String cityIdToSelect = prefillCityId;
                                    prefillCityId = ""; // reset after first use
                                    fetchCityList(selectedStateId, cityIdToSelect);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                }
            }

            @Override
            public void onFailure(Call<StateListResponse> call, Throwable t) {
                dialog.dismiss();
                Log.e(TAG, "State API failed: " + t.getMessage());
            }
        });
    }

    // ================= CITY API =================

    private void fetchCityList(String stateId, String selectCityId) {
        apiService.getCityList(new CityListRequest(stateId))
                .enqueue(new Callback<CityListResponse>() {
                    @Override
                    public void onResponse(Call<CityListResponse> call,
                                           Response<CityListResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            cityList = response.body().getJData();

                            // Remove nulls
                            Iterator<CityListResponse.CityData> it = cityList.iterator();
                            while (it.hasNext()) {
                                if (it.next().getCCityName() == null) it.remove();
                            }

                            ArrayAdapter<CityListResponse.CityData> adapter =
                                    new ArrayAdapter<>(DeliveryAddressActivity.this,
                                            R.layout.spinner_item, cityList);
                            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                            spinnerCity.setAdapter(adapter);

                            // ✅ Prefill city by ID
                            if (selectCityId != null && !selectCityId.isEmpty()) {
                                for (int i = 0; i < cityList.size(); i++) {
                                    if (cityList.get(i).getNId().equals(selectCityId)) {
                                        spinnerCity.setSelection(i);
                                        selectedCityId = selectCityId;
                                        break;
                                    }
                                }
                            } else if (pendingCity != null) {
                                setSpinnerByText(spinnerCity, pendingCity);
                                pendingCity = null;
                            }

                            // Track selected city
                            spinnerCity.setOnItemSelectedListener(
                                    new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent,
                                                                   android.view.View view,
                                                                   int pos, long id) {
                                            selectedCityId = cityList.get(pos).getNId();
                                        }
                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {}
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Call<CityListResponse> call, Throwable t) {
                        Log.e(TAG, "City API failed: " + t.getMessage());
                    }
                });
    }

    // ================= SAVE =================

    private void saveData() {
        String address = etAddressLine.getText().toString().trim();
        String pin     = etPinCode.getText().toString().trim();

        // Validations
        if (lat == 0.0 || lng == 0.0) {
            Toast.makeText(this, "Please select a location on map",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) {
            etAddressLine.setError("Enter address");
            etAddressLine.requestFocus();
            return;
        }

        if (pin.isEmpty() || pin.length() < 6) {
            etPinCode.setError("Enter valid 6-digit PIN");
            etPinCode.requestFocus();
            return;
        }

        if (selectedStateId.isEmpty()) {
            Toast.makeText(this, "Please select state", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCityId.isEmpty()) {
            Toast.makeText(this, "Please select city", Toast.LENGTH_SHORT).show();
            return;
        }

        // Address type: index 0=Home(1), 1=Office(2), 2=Other(3)
        String addrType = String.valueOf(
                spinnerAddressType.getSelectedItemPosition() + 1);

        dialog.setMessage("Saving address...");
        dialog.show();

        // ✅ Correct parameter order matching RegisterInsertRequest.forAddress()
        RegisterInsertRequest req = RegisterInsertRequest.forAddress(
                mobileNumber,           // mobile
                addrType,               // addressType
                pin,                    // pincode
                address,                // address
                selectedStateId,        // stateId
                selectedCityId,         // cityId
                String.valueOf(lat),    // latitude
                String.valueOf(lng)     // longitude
        );

        apiService.registerInsert(req).enqueue(new Callback<RegisterInsertResponse>() {
            @Override
            public void onResponse(Call<RegisterInsertResponse> call,
                                   Response<RegisterInsertResponse> res) {
                dialog.dismiss();

                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(DeliveryAddressActivity.this,
                            "Failed to save. Try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                RegisterInsertResponse body = res.body();
                Log.d(TAG, "Address save → status=" + body.getNStatus()
                        + " msg=" + body.getCMessage());

                if (body.getNStatus() != 1) {
                    Toast.makeText(DeliveryAddressActivity.this,
                            body.getCMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Success → LoginPage
                Toast.makeText(DeliveryAddressActivity.this,
                        "Registration complete!", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(DeliveryAddressActivity.this, LoginPage.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<RegisterInsertResponse> call, Throwable t) {
                dialog.dismiss();
                Log.e(TAG, "Address save failed: " + t.getMessage());
                Toast.makeText(DeliveryAddressActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= HELPERS =================

    private void setSpinnerByText(Spinner spinner, String text) {
        if (text == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            Object item = adapter.getItem(i);
            if (item != null && item.toString().equalsIgnoreCase(text)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}