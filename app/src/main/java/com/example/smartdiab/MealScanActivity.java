package com.example.smartdiab;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class MealScanActivity extends AppCompatActivity {

    private View scanLine;
    private View analyzingLayout;
    private ImageButton captureBtn, closeScanBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_scan);

        scanLine = findViewById(R.id.scanLine);
        analyzingLayout = findViewById(R.id.analyzingLayout);
        captureBtn = findViewById(R.id.captureBtn);
        closeScanBtn = findViewById(R.id.closeScanBtn);

        startScanAnimation();

        captureBtn.setOnClickListener(v -> {
            analyzingLayout.setVisibility(View.VISIBLE);
            // Simulate AI Processing
            new Handler().postDelayed(() -> {
                startActivity(new Intent(MealScanActivity.this, MealResultActivity.class));
                finish();
            }, 3000);
        });

        closeScanBtn.setOnClickListener(v -> finish());
    }

    private void startScanAnimation() {
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.8f
        );
        animation.setDuration(2000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        scanLine.startAnimation(animation);
    }
}