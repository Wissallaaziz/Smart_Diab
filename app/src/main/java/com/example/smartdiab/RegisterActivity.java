package com.example.smartdiab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputEditText email, password, name;
    private Button registerBtn;
    private TextView loginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        registerBtn = findViewById(R.id.registerBtn);
        loginBtn = findViewById(R.id.loginText);

        registerBtn.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            String n = name.getText().toString().trim();

            if (!validateInput(n, e, p)) return;

            Log.d(TAG, "Tentative d'inscription pour : " + e);
            
            mAuth.createUserWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Inscription réussie !");
                            Toast.makeText(this, "Compte créé avec succès", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
                            Log.e(TAG, "Échec de l'inscription : " + errorMsg);
                            Toast.makeText(this, "Erreur : " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        loginBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateInput(String nameStr, String emailStr, String passwordStr) {
        if (nameStr.isEmpty()) {
            name.setError("Nom requis");
            name.requestFocus();
            return false;
        }
        if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("Email invalide");
            email.requestFocus();
            return false;
        }
        if (passwordStr.length() < 6) {
            password.setError("Minimum 6 caractères");
            password.requestFocus();
            return false;
        }
        return true;
    }
}