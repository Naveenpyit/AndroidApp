package com.example.myapplication.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

public class DeliveryAddressActivity extends AppCompatActivity implements OnMapReadyCallback {

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

    // ── Intent data ───────────────────────────────────────────────────────────
    private String fullName, mobileNumber, email, businessType, gstNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        setupStatusBar();
        getIntentData();
        initializeViews();
        setupSpinners();
        setupClickListeners();
        setupMap();
        requestLocationPermission();
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
        setSpinner(spinnerCity,        R.array.cities);
        setSpinner(spinnerState,       R.array.states);
        setSpinner(spinnerCountry,     R.array.countries);
    }

    private void setSpinner(Spinner spinner, int arrayRes) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayRes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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