package com.example.smartdiab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private TextView registerText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // UI
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerText = findViewById(R.id.registerText);

        // 🔐 LOGIN
        loginBtn.setOnClickListener(v -> loginUser());

        // 🔁 Aller vers Register
        registerText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    // 🔐 Fonction login propre
    private void loginUser() {
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();

        if (!validateInput(e, p)) return;

        mAuth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                            // Redirection selon l'état du questionnaire spécifique à l'utilisateur
                            SharedPreferences prefs = getSharedPreferences("SmartDiab", MODE_PRIVATE);
                            boolean isDone = prefs.getBoolean("isDone_" + user.getUid(), false);

                            Intent intent;
                            if (isDone) {
                                intent = new Intent(LoginActivity.this, HomeActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, QuestionnaireActivity.class);
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Erreur : " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ✅ Validation
    private boolean validateInput(String emailVal, String passwordVal) {
        if (emailVal.isEmpty()) {
            email.setError("Email requis");
            email.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
            email.setError("Email invalide");
            email.requestFocus();
            return false;
        }
        if (passwordVal.isEmpty()) {
            password.setError("Mot de passe requis");
            password.requestFocus();
            return false;
        }
        if (passwordVal.length() < 6) {
            password.setError("Minimum 6 caractères");
            password.requestFocus();
            return false;
        }
        return true;
    }

    // 🔄 Auto-login uniquement si l'utilisateur est connecté ET a fini le questionnaire
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            SharedPreferences prefs = getSharedPreferences("SmartDiab", MODE_PRIVATE);
            boolean isDone = prefs.getBoolean("isDone_" + currentUser.getUid(), false);

            if (isDone) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, QuestionnaireActivity.class));
                finish();
            }
        }
    }
}