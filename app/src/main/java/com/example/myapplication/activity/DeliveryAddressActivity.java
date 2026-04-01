package com.example.myapplication.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.CityListRequest;
import com.example.myapplication.model.CityListResponse;
import com.example.myapplication.model.StateListResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryAddressActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "DeliveryAddressActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // ── Views ─────────────────────────────────────────────────────────────────
    private ImageView backArrow;
    private TextView tvCurrentLocation;
    private MaterialButton btnUseLocation, btnContinue;
    private RadioGroup rgAddressType;
    private Spinner spinnerAddressType, spinnerCity, spinnerState, spinnerCountry;
    private EditText etAddressLine, etPinCode;

    // ── Map & Location ────────────────────────────────────────────────────────
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 20.5937, currentLng = 78.9629; // Default: India center
    private String resolvedAddress = "";

    // ── API & Data ────────────────────────────────────────────────────────────
    private ApiService apiService;
    private List<StateListResponse.StateData> stateList = new ArrayList<>();
    private List<CityListResponse.CityData> cityList = new ArrayList<>();
    private String selectedStateId = "";

    // ── Intent data ───────────────────────────────────────────────────────────
    private String fullName, mobileNumber, email, businessType, gstNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        setupStatusBar();
        getIntentData();
        initializeViews();
        apiService = RetrofitClient.getClient(this);
        setupSpinners();
        setupClickListeners();
        setupMap();
        requestLocationPermission();
        fetchStateList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────────────────

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

    private void getIntentData() {
        fullName     = getIntent().getStringExtra("fullName");
        mobileNumber = getIntent().getStringExtra("mobileNumber");
        email        = getIntent().getStringExtra("email");
        businessType = getIntent().getStringExtra("businessType");
        gstNumber    = getIntent().getStringExtra("gstNumber");
    }

    private void initializeViews() {
        backArrow          = findViewById(R.id.back_arrow);
        tvCurrentLocation  = findViewById(R.id.tv_current_location);
        btnUseLocation     = findViewById(R.id.btn_use_location);
        btnContinue        = findViewById(R.id.btn_continue);
        rgAddressType      = findViewById(R.id.rg_address_type);
        spinnerAddressType = findViewById(R.id.spinner_address_type);
        spinnerCity        = findViewById(R.id.spinner_city);
        spinnerState       = findViewById(R.id.spinner_state);
        spinnerCountry     = findViewById(R.id.spinner_country);
        etAddressLine      = findViewById(R.id.et_address_line);
        etPinCode          = findViewById(R.id.et_pin_code);
    }

    private void setupSpinners() {
        setSpinner(spinnerAddressType, R.array.address_types);
        setSpinner(spinnerCountry,     R.array.countries);
        
        // Initialize state and city spinners with loading state
        List<String> loadingState = new ArrayList<>();
        loadingState.add("Loading States...");
        ArrayAdapter<String> stateLoadingAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, loadingState);
        stateLoadingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(stateLoadingAdapter);
        spinnerState.setEnabled(false);
        
        List<String> loadingCity = new ArrayList<>();
        loadingCity.add("Select State First");
        ArrayAdapter<String> cityLoadingAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, loadingCity);
        cityLoadingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityLoadingAdapter);
        spinnerCity.setEnabled(false);
        
        // Add listener for state selection to fetch cities
        spinnerState.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position > 0 && !stateList.isEmpty()) {
                    StateListResponse.StateData selectedState = stateList.get(position - 1);
                    selectedStateId = selectedState.getNId();
                    Log.d(TAG, "State selected: " + selectedState.getCStateName() + " (ID: " + selectedStateId + ")");
                    fetchCityList(selectedStateId);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setSpinner(Spinner spinner, int arrayRes) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayRes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API Methods
    // ─────────────────────────────────────────────────────────────────────────

    private void fetchStateList() {
        Log.d(TAG, "Fetching state list...");
        apiService.getStateList().enqueue(new Callback<StateListResponse>() {
            @Override
            public void onResponse(Call<StateListResponse> call, Response<StateListResponse> response) {
                Log.d(TAG, "State List API Response received. Code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    StateListResponse stateResponse = response.body();
                    Log.d(TAG, "State List Response: Status=" + stateResponse.getNStatus() + 
                            ", Message=" + stateResponse.getCMessage() + 
                            ", Data size=" + (stateResponse.getJData() != null ? stateResponse.getJData().size() : 0));
                    
                    if (stateResponse.getNStatus() == 1 && stateResponse.getJData() != null) {
                        stateList = stateResponse.getJData();
                        Log.d(TAG, "✓ States fetched successfully: " + stateList.size() + " states");
                        for (int i = 0; i < Math.min(3, stateList.size()); i++) {
                            Log.d(TAG, "  State " + i + ": " + stateList.get(i).getCStateName() + " (ID: " + stateList.get(i).getNId() + ")");
                        }
                        updateStateSpinner();
                    } else {
                        Log.e(TAG, "✗ State API error: Status=" + stateResponse.getNStatus() + ", Message=" + stateResponse.getCMessage());
                        Toast.makeText(DeliveryAddressActivity.this, 
                                "Failed to fetch states: " + stateResponse.getCMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody().string();
                    } catch (Exception e) {
                        errorBody = e.getMessage();
                    }
                    Log.e(TAG, "✗ State API failed: Code=" + response.code() + ", Error=" + errorBody);
                    Toast.makeText(DeliveryAddressActivity.this, 
                            "Error fetching states (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StateListResponse> call, Throwable t) {
                Log.e(TAG, "✗ State API Failure: " + t.getMessage(), t);
                Toast.makeText(DeliveryAddressActivity.this, 
                        "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCityList(String stateId) {
        Log.d(TAG, "Fetching city list for state ID: " + stateId);
        CityListRequest request = new CityListRequest("1");
        apiService.getCityList(request).enqueue(new Callback<CityListResponse>() {
            @Override
            public void onResponse(Call<CityListResponse> call, Response<CityListResponse> response) {
                Log.d(TAG, "City List API Response received. Code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    CityListResponse cityResponse = response.body();
                    Log.d(TAG, "City List Response: Status=" + cityResponse.getNStatus() + 
                            ", Message=" + cityResponse.getCMessage() + 
                            ", Data size=" + (cityResponse.getJData() != null ? cityResponse.getJData().size() : 0));
                    
                    if (cityResponse.getNStatus() == 1 && cityResponse.getJData() != null) {
                        cityList = cityResponse.getJData();
                        Log.d(TAG, "✓ Cities fetched successfully: " + cityList.size() + " cities");
                        for (int i = 0; i < Math.min(3, cityList.size()); i++) {
                            Log.d(TAG, "  City " + i + ": " + cityList.get(i).getCCityName() + " (ID: " + cityList.get(i).getNId() + ")");
                        }
                        updateCitySpinner();
                    } else {
                        Log.e(TAG, "✗ City API error: Status=" + cityResponse.getNStatus() + ", Message=" + cityResponse.getCMessage());
                        Toast.makeText(DeliveryAddressActivity.this, 
                                "Failed to fetch cities: " + cityResponse.getCMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody().string();
                    } catch (Exception e) {
                        errorBody = e.getMessage();
                    }
                    Log.e(TAG, "✗ City API failed: Code=" + response.code() + ", Error=" + errorBody);
                    Toast.makeText(DeliveryAddressActivity.this, 
                            "Error fetching cities (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CityListResponse> call, Throwable t) {
                Log.e(TAG, "✗ City API Failure: " + t.getMessage(), t);
                Toast.makeText(DeliveryAddressActivity.this, 
                        "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStateSpinner() {
        List<String> stateNames = new ArrayList<>();
        stateNames.add("Select State");
        for (StateListResponse.StateData state : stateList) {
            stateNames.add(state.getCStateName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, stateNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(adapter);
        spinnerState.setEnabled(true);
        Log.d(TAG, "State spinner updated with " + stateNames.size() + " items");
        Toast.makeText(DeliveryAddressActivity.this, (stateNames.size() - 1) + " states loaded", Toast.LENGTH_SHORT).show();
    }

    private void updateCitySpinner() {
        List<String> cityNames = new ArrayList<>();
        cityNames.add("Select City");
        for (CityListResponse.CityData city : cityList) {
            cityNames.add(city.getCCityName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, cityNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);
        spinnerCity.setEnabled(true);
        Log.d(TAG, "City spinner updated with " + cityNames.size() + " items");
        Toast.makeText(DeliveryAddressActivity.this, (cityNames.size() - 1) + " cities loaded", Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        backArrow.setOnClickListener(v -> onBackPressed());
        btnContinue.setOnClickListener(v -> validateAndContinue());

        // "Use" button — fills address fields from current GPS location
        btnUseLocation.setOnClickListener(v -> {
            if (!resolvedAddress.isEmpty()) {
                etAddressLine.setText(resolvedAddress);
                Toast.makeText(this, "Location applied", Toast.LENGTH_SHORT).show();
            } else {
                requestLocationPermission();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Google Map
    // ─────────────────────────────────────────────────────────────────────────

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Move to default/current location
        LatLng defaultLocation = new LatLng(currentLat, currentLng);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        // Allow user to tap map to pick location
        googleMap.setOnMapClickListener(latLng -> {
            currentLat = latLng.latitude;
            currentLng = latLng.longitude;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            reverseGeocode(latLng.latitude, latLng.longitude);
        });

        // Enable my location layer if permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GPS Location
    // ─────────────────────────────────────────────────────────────────────────

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            fetchCurrentLocation();
        }
    }

    private void fetchCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        tvCurrentLocation.setText("Fetching location...");

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();

                // Move map to current location
                if (googleMap != null) {
                    LatLng latLng = new LatLng(currentLat, currentLng);
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng).title("Your Location"));
                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                }

                reverseGeocode(currentLat, currentLng);

            } else {
                tvCurrentLocation.setText("Unable to get location. Tap on map.");
            }
        }).addOnFailureListener(e ->
                tvCurrentLocation.setText("Location error: " + e.getMessage()));
    }

    private void reverseGeocode(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) sb.append(", ");
                    }
                    resolvedAddress = sb.toString();

                    String city    = address.getLocality();
                    String state   = address.getAdminArea();
                    String pinCode = address.getPostalCode();

                    runOnUiThread(() -> {
                        tvCurrentLocation.setText(resolvedAddress);

                        // Auto-fill PIN code if found
                        if (pinCode != null && !pinCode.isEmpty()) {
                            etPinCode.setText(pinCode);
                        }

                        // Auto-select city in spinner
                        if (city != null) selectSpinnerItem(spinnerCity, city);

                        // Auto-select state in spinner
                        if (state != null) selectSpinnerItem(spinnerState, state);
                    });
                } else {
                    runOnUiThread(() ->
                            tvCurrentLocation.setText("Lat: " + lat + ", Lng: " + lng));
                }
            } catch (IOException e) {
                runOnUiThread(() ->
                        tvCurrentLocation.setText("Address lookup failed"));
            }
        }).start();
    }

    private void selectSpinnerItem(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString()
                    .equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
                if (googleMap != null) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                }
            } else {
                Toast.makeText(this,
                        "Location permission denied. Please enable it in Settings.",
                        Toast.LENGTH_LONG).show();
                tvCurrentLocation.setText("Location permission denied");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Validation
    // ─────────────────────────────────────────────────────────────────────────

    private void validateAndContinue() {
        String addressLine = etAddressLine.getText().toString().trim();
        String pinCode     = etPinCode.getText().toString().trim();
        String city        = spinnerCity.getSelectedItem() != null
                ? spinnerCity.getSelectedItem().toString() : "";
        String state       = spinnerState.getSelectedItem() != null
                ? spinnerState.getSelectedItem().toString() : "";

        if (addressLine.isEmpty()) {
            etAddressLine.setError("Please enter address line");
            etAddressLine.requestFocus();
            return;
        }

        if (pinCode.isEmpty() || pinCode.length() < 6) {
            etPinCode.setError("Enter valid 6-digit PIN code");
            etPinCode.requestFocus();
            return;
        }

        if (city.equals("Select City") || city.isEmpty()) {
            Toast.makeText(this, "Please select city", Toast.LENGTH_SHORT).show();
            return;
        }

        if (state.equals("Select State") || state.isEmpty()) {
            Toast.makeText(this, "Please select state", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Account setup completed!", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(DeliveryAddressActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}