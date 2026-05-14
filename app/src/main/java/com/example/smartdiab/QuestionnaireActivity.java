package com.example.smartdiab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class QuestionnaireActivity extends AppCompatActivity {

    ImageView questionImage;
    TextView questionText;
    Button btnYes, btnNo;
    LinearProgressIndicator progressIndicator;

    int currentQuestion = 0;
    int scoreType1 = 0;
    int scoreType2 = 0;

    String[] questions = {
            "Avez-vous une perte de poids rapide ?",
            "Ressentez-vous une fatigue intense ?",
            "Faites-vous peu d'activité physique ?",
            "Consommez-vous beaucoup de sucre ?",
            "Avez-vous souvent soif ?",
            "Urinez-vous fréquemment ?",
            "Avez-vous des antécédents familiaux ?",
            "Votre poids est-il élevé ?",
            "Avez-vous une vision floue ?",
            "Avez-vous une faim excessive ?"
    };

    int[] images = {
            R.drawable.q1, R.drawable.q2, R.drawable.q3,
            R.drawable.q4, R.drawable.q5, R.drawable.q6,
            R.drawable.q7, R.drawable.q8, R.drawable.q9,
            R.drawable.q10
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        questionImage = findViewById(R.id.questionImage);
        questionText = findViewById(R.id.questionText);
        btnYes = findViewById(R.id.btn1);
        btnNo = findViewById(R.id.btn2);
        progressIndicator = findViewById(R.id.questionProgress);

        loadQuestion();

        btnYes.setOnClickListener(v -> answer(true));
        btnNo.setOnClickListener(v -> answer(false));
    }

    private void loadQuestion() {
        if (currentQuestion < questions.length) {
            questionText.setText(questions[currentQuestion]);
            questionImage.setImageResource(images[currentQuestion]);
            
            if (progressIndicator != null) {
                int progress = (int) (((float) (currentQuestion + 1) / questions.length) * 100);
                progressIndicator.setProgress(progress);
            }
        }
    }

    private void answer(boolean yes) {
        if (yes) {
            if (currentQuestion <= 1 || currentQuestion == 9) {
                scoreType1++;
            } else {
                scoreType2++;
            }
        }

        currentQuestion++;

        if (currentQuestion < questions.length) {
            loadQuestion();
        } else {
            showResult();
        }
    }

    private void showResult() {
        String result;
        if (scoreType1 > scoreType2) {
            result = "Diabète Type 1";
        } else {
            result = "Diabète Type 2";
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SharedPreferences prefs = getSharedPreferences("SmartDiab", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("diabeteType_" + user.getUid(), result);
            editor.putBoolean("isDone_" + user.getUid(), true);
            editor.apply();
        }

        Intent intent = new Intent(QuestionnaireActivity.this, HomeActivity.class);
        intent.putExtra("result", result);
        startActivity(intent);
        finish();
    }
}
