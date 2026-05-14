package com.example.smartdiab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String TAG = "HomeActivity";

    // UI & Avatar
    private MaterialButton scanBtn, logoutBtn, aiBtn;
    private TextView resultText;
    private LottieAnimationView avatarAnimation;
    private TextToSpeech tts;

    // Maps
    private MapView map = null;
    private MyLocationNewOverlay locationOverlay;

    private String currentPhotoPath;
    private String userDiabeteType = "Type 2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. CONFIGURATION OSMDROID (Indispensable pour l'affichage de la carte)
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_home);

        // 2. INITIALISATION
        initViews();
        setupTTS();
        setupUserContext();
        setupMap();

        // 3. LISTENERS
        setupListeners();

        // 4. VERIFICATION PERMISSIONS
        checkPermissions();
    }

    private void initViews() {
        scanBtn = findViewById(R.id.scanBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        aiBtn = findViewById(R.id.aiBtn);
        resultText = findViewById(R.id.resultText);
        avatarAnimation = findViewById(R.id.avatarAnimation);
        map = findViewById(R.id.map);
    }

    private void setupTTS() {
        tts = new TextToSpeech(this, this);
    }

    private void setupUserContext() {
        SharedPreferences prefs = getSharedPreferences("SmartDiab", MODE_PRIVATE);
        userDiabeteType = prefs.getString("diabeteType_" + FirebaseAuth.getInstance().getUid(), "Type 2");
        resultText.setText("Votre profil : " + userDiabeteType);
    }

    private void setupMap() {
        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
            map.getController().setZoom(15.0);
            map.getController().setCenter(new GeoPoint(33.5731, -7.5898)); // Casablanca

            locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
            map.getOverlays().add(locationOverlay);
        }
    }

    private void setupListeners() {
        scanBtn.setOnClickListener(v -> openCamera());

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

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = File.createTempFile("IMG_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            currentPhotoPath = photoFile.getAbsolutePath();
            Uri photoURI = FileProvider.getUriForFile(this, "com.example.smartdiab.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            Log.e(TAG, "Erreur création fichier", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            sendToBackend();
        }
    }

    private void sendToBackend() {
        avatarAnimation.setAnimation("loading_avatar.json");
        avatarAnimation.playAnimation();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.56.1:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SmartDiabApi api = retrofit.create(SmartDiabApi.class);
        File file = new File(currentPhotoPath);
        RequestBody rb = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), rb);
        RequestBody type = RequestBody.create(MediaType.parse("text/plain"), userDiabeteType);

        api.analyzeMeal(type, part).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        handleVerdict(response.body().string());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur réponse", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Connexion impossible au PC", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleVerdict(String result) {
        String speechText;
        if (result.contains("Attention") || result.contains("éviter")) {
            avatarAnimation.setAnimation("warning_avatar.json");
            speechText = "Prudence. Ce repas contient trop de sucre pour votre profil.";
        } else {
            avatarAnimation.setAnimation("healthy_avatar.json");
            speechText = "Analyse terminée. Ce plat semble adapté. Bon appétit !";
        }

        avatarAnimation.playAnimation();
        tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "verdict_speech");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Analyse de l'IA")
                .setMessage(result)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.FRENCH);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}