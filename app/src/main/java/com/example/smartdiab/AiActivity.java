package com.example.smartdiab;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import java.util.Locale;

public class AiActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText userInput;
    private TextView aiResponse;
    private LottieAnimationView aiAvatar;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        userInput = findViewById(R.id.userInput);
        aiResponse = findViewById(R.id.aiResponse);
        aiAvatar = findViewById(R.id.aiAvatar);
        Button sendBtn = findViewById(R.id.sendBtn);

        tts = new TextToSpeech(this, this);

        sendBtn.setOnClickListener(v -> {
            String question = userInput.getText().toString();
            if (!question.isEmpty()) {
                askTinyLlama(question);
            }
        });
    }

    private void askTinyLlama(String question) {
        // Animation de réflexion - Using existing Doctor_Avatar.json
        aiAvatar.setAnimation("Doctor_Avatar.json");
        aiAvatar.playAnimation();

        // Ici, tu appelles ton service local ou une route spécifique de ton backend
        String response = "En tant qu'assistant SmartDiab, je vous conseille de surveiller votre glycémie après ce repas.";

        aiResponse.setText(response);

        // L'avatar parle et change d'humeur
        aiAvatar.setAnimation("healthy_avatar.json");
        aiAvatar.playAnimation();
        tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.FRENCH);
    }
}