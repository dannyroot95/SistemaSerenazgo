package com.optic.sistemaSerenazgo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.includes.MyToolbar;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class ForgotPassword extends AppCompatActivity {

    MaterialButton mSubmit;
    TextInputEditText etdEmail;
    private FirebaseAuth mAuth;
    ProgressBar dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        dialog = findViewById(R.id.forgot_bar);
        mAuth = FirebaseAuth.getInstance();
        mSubmit = findViewById(R.id.btn_submit_email);
        etdEmail = findViewById(R.id.et_email);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecoveryPassword();
            }
        });


    }

    private void RecoveryPassword(){
        mSubmit.setEnabled(false);
        dialog.setVisibility(View.VISIBLE);
        String email = Objects.requireNonNull(etdEmail.getText()).toString();
        if(TextUtils.isEmpty(email)){
            dialog.setVisibility(View.GONE);
            mSubmit.setEnabled(true);
            Toasty.warning(getApplication(), "Ingrese su correo electronico", Toast.LENGTH_SHORT,true).show();
        }

        else{
            mAuth.setLanguageCode("es");
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(ForgotPassword.this,MainActivity.class);
                        intent.putExtra("send","success");
                        startActivity(intent);
                        finish();
                    }
                    else{
                        dialog.setVisibility(View.GONE);
                        mSubmit.setEnabled(true);
                        Toasty.error(ForgotPassword.this,"Error al enviar correo electronico", Toast.LENGTH_SHORT,true).show();
                    }
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        if (dialog.getVisibility() == View.VISIBLE){
            Toasty.error(this, "Espere un momento...", Toast.LENGTH_SHORT).show();
        }
        else{
            super.onBackPressed();
        }
    }
}