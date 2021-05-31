package com.optic.sistemaSerenazgo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NavUtils;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.activities.serene.MapSereneActivity;
import com.optic.sistemaSerenazgo.activities.patrol.MapPatrolActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    CardView mButtonIAmClient ,  mButtonIAmDriver;
    SharedPreferences mPref;
    ProgressBar progressBar;
    ProgressBar progressBar2;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeWhite);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_email);

        Intent intent = getIntent();
        String val = intent.getStringExtra("send");

        if (Objects.equals(val, "success")){
            setupDialogEmail();
        }

        //CREAMOS LA VARIABLE mPref PARA GUARDAR EN CACHÉ SEGUN LA OPCION SE QUE HALLA ESCOGIDO
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        final SharedPreferences.Editor editor = mPref.edit();

        progressBar = findViewById(R.id.progress);
        progressBar2 = findViewById(R.id.progress2);
        mButtonIAmClient = findViewById(R.id.btnIAmClient);
        mButtonIAmDriver = findViewById(R.id.btnIAmDriver);

        //CREAMOS UNA KEY E SE INICALIZA EL ACTIVITY Y SE GUARDA EN CACHÉ SEA UN (client ó driver)
        mButtonIAmClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                editor.putString("user", "client");
                editor.apply();
                goToSelectAuth();
            }
        });

        mButtonIAmDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar2.setVisibility(View.VISIBLE);
                editor.putString("user", "driver");
                editor.apply();
                goToSelectAuth();
            }
        });
    }

    // VERIFICAMOS SI INICIAMOS SESION O NO Y TAMBIEN EL TIPO DE USUARIO SEGUN LA KEY QUE SE ALMACENÓ EN CACHÉ
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String user = mPref.getString("user", "");
            if (user.equals("client")) {
                Intent intent = new Intent(MainActivity.this, MapSereneActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(MainActivity.this, MapPatrolActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    //METODO PARA SABER QUE OPCION SE PRESIONÓ Y SE LANZA LA ACTIVITY CORRESPONDIENTE
    private void goToSelectAuth() {
        disable();
        Intent intent = new Intent(MainActivity.this, SelectOptionAuthActivity.class);
        startActivity(intent);
        //progressBar.setVisibility(View.GONE);
    }

    private void enable(){
      mButtonIAmClient.setEnabled(true);
      mButtonIAmDriver.setEnabled(true);
    }
    private void disable(){
        mButtonIAmClient.setEnabled(false);
        mButtonIAmDriver.setEnabled(false);
    }

    private void setupDialogEmail(){
        ImageView close_dialog = dialog.findViewById(R.id.closeDialog);
        Button btnOK = dialog.findViewById(R.id.btn_ok);
        dialog.show();
        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        enable();
        progressBar.setVisibility(View.INVISIBLE);
        progressBar2.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (progressBar.getVisibility() == View.VISIBLE || progressBar2.getVisibility() == View.VISIBLE){
        }
        else{
            super.onBackPressed();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

    }
}
