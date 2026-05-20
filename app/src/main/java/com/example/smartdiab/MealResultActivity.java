package com.example.smartdiab;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;

public class MealResultActivity extends AppCompatActivity {

    private TextView resMealName, resVerdict, resCarbs, resImpact, resAdvice;
    private LottieAnimationView resultAvatar;
    private MaterialButton finishBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_result);

        resMealName = findViewById(R.id.resMealName);
        resVerdict = findViewById(R.id.resVerdict);
        resCarbs = findViewById(R.id.resCarbs);
        resImpact = findViewById(R.id.resImpact);
        resAdvice = findViewById(R.id.resAdvice);
        resultAvatar = findViewById(R.id.resultAvatar);
        finishBtn = findViewById(R.id.finishBtn);

        // Mocking the result for UI demonstration
        // In a real app, these would come from an Intent extra or API call
        displayResult("Salade de Quinoa", "24g", "Bas", "Autorisé", 
            "Ce repas est excellent pour maintenir votre glycémie stable. Vous pouvez le consommer sans inquiétude.");

        finishBtn.setOnClickListener(v -> finish());
    }

    private void displayResult(String name, String carbs, String impact, String verdict, String advice) {
        resMealName.setText(name);
        resCarbs.setText(carbs);
        resImpact.setText(impact);
        resVerdict.setText(verdict);
        resAdvice.setText(advice);

        if (verdict.contains("Autorisé")) {
            resVerdict.setTextColor(getResources().getColor(R.color.accent_green));
            resultAvatar.setAnimation("healthy_avatar.json");
        } else if (verdict.contains("Modéré")) {
            resVerdict.setTextColor(getResources().getColor(R.color.status_moderate));
            resultAvatar.setAnimation("Doctor_Avatar.json");
        } else {
            resVerdict.setTextColor(getResources().getColor(R.color.status_avoid));
            resultAvatar.setAnimation("warning_avatar.json");
        }
        resultAvatar.playAnimation();
    }
}