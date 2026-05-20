package com.example.smartdiab;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private TextView userNameText, diabeteTypeLabel;
    private MaterialButton scanBtn, mapBtn, logoutBtn;
    private ExtendedFloatingActionButton aiBtn;
    private MapView map;
    private MyLocationNewOverlay locationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid Configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_home);

        initViews();
        setupUserContext();
        setupMap();
        setupListeners();
        checkPermissions();
    }

    private void initViews() {
        userNameText = findViewById(R.id.userNameText);
        diabeteTypeLabel = findViewById(R.id.diabeteTypeLabel);
        scanBtn = findViewById(R.id.scanBtn);
        mapBtn = findViewById(R.id.mapBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        aiBtn = findViewById(R.id.aiBtn);
        map = findViewById(R.id.map);
    }

    private void setupUserContext() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            userNameText.setText(name != null && !name.isEmpty() ? name : "Utilisateur");

            SharedPreferences prefs = getSharedPreferences("SmartDiab", MODE_PRIVATE);
            String type = prefs.getString("diabeteType_" + user.getUid(), "Type 2");
            diabeteTypeLabel.setText(type);
        }
    }

    private void setupMap() {
        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
            map.getController().setZoom(15.0);
            map.getController().setCenter(new GeoPoint(33.5731, -7.5898)); // Casablanca

            locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
            map.getOverlays().add(locationOverlay);
        }
    }

    private void setupListeners() {
        scanBtn.setOnClickListener(v -> {
            // Logique de scan (Camera) - Redirection ou ouverture directe
            Toast.makeText(this, "Ouverture de la caméra...", Toast.LENGTH_SHORT).show();
        });

        mapBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MapsActivity.class));
        });

        aiBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AiActivity.class));
        });

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
            locationOverlay.disableFollowLocation();
        }
    }
}