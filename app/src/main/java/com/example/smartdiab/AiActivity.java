package com.example.smartdiab;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

public class AiActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SPEECH_TO_TEXT = 2;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private EditText userInput;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private LottieAnimationView aiAvatar;
    private TextView diabetesTypeHeader;
    private TextToSpeech tts;
    private String userDiabeteType = "Type 2";
    private SmartDiabApi api;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString("photoPath");
        }

        // UI
        userInput = findViewById(R.id.userInput);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        aiAvatar = findViewById(R.id.aiAvatar);
        diabetesTypeHeader = findViewById(R.id.diabetesTypeHeader);
        FloatingActionButton sendBtn = findViewById(R.id.sendBtn);
        
        MaterialButton openCameraBtn = findViewById(R.id.openCameraBtn);
        MaterialButton micBtn = findViewById(R.id.micBtn);

        // Chat List
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        tts = new TextToSpeech(this, this);

        // Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.56.1:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SmartDiabApi.class);

        // User Data
        SharedPreferences prefs = getSharedPreferences("SmartDiab", MODE_PRIVATE);
        userDiabeteType = prefs.getString("diabeteType_" + FirebaseAuth.getInstance().getUid(), "Type 2");
        diabetesTypeHeader.setText("Profil : " + userDiabeteType);

        // Listeners
        sendBtn.setOnClickListener(v -> {
            String question = userInput.getText().toString().trim();
            if (!question.isEmpty()) {
                addMessage(question, true);
                userInput.setText("");
                askTinyLlama(question);
            }
        });

        openCameraBtn.setOnClickListener(v -> checkCameraPermission());
        micBtn.setOnClickListener(v -> startVoiceRecognition());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photoPath", currentPhotoPath);
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new Message(text, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void askTinyLlama(String question) {
        aiAvatar.setAnimation("Doctor_Avatar.json");
        aiAvatar.playAnimation();

        api.askAi(question, userDiabeteType).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        handleAiResponse(response.body().string());
                    } else {
                        handleAiResponse("Je ne parviens pas à traiter votre demande.");
                    }
                } catch (Exception e) {
                    handleAiResponse("Erreur de lecture de la réponse.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleAiResponse("Erreur : Connexion au serveur impossible.");
            }
        });
    }

    private void handleAiResponse(String response) {
        addMessage(response, false);
        if (response.toLowerCase().contains("attention") || response.toLowerCase().contains("éviter")) {
            aiAvatar.setAnimation("warning_avatar.json");
        } else {
            aiAvatar.setAnimation("healthy_avatar.json");
        }
        aiAvatar.playAnimation();
        tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, "ai_speech");
    }

    // --- CAMERA LOGIC ---
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = File.createTempFile("AI_IMG_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            currentPhotoPath = photoFile.getAbsolutePath();
            Uri photoURI = FileProvider.getUriForFile(this, "com.example.smartdiab.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            Toast.makeText(this, "Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show();
        }
    }

    // --- VOICE LOGIC ---
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH);
        try {
            startActivityForResult(intent, REQUEST_SPEECH_TO_TEXT);
        } catch (Exception e) {
            Toast.makeText(this, "Microphone non supporté", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                addMessage("[Image envoyée pour analyse]", true);
                analyzeImage();
            } else if (requestCode == REQUEST_SPEECH_TO_TEXT && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    userInput.setText(result.get(0));
                }
            }
        }
    }

    private void analyzeImage() {
        if (currentPhotoPath == null) {
            Toast.makeText(this, "Erreur : image introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        aiAvatar.setAnimation("Doctor_Avatar.json");
        aiAvatar.playAnimation();

        File file = new File(currentPhotoPath);
        RequestBody rb = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), rb);
        RequestBody type = RequestBody.create(MediaType.parse("text/plain"), userDiabeteType);

        api.analyzeMeal(type, part).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        handleAiResponse(response.body().string());
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleAiResponse("Échec de l'analyse de l'image.");
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.FRENCH);
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