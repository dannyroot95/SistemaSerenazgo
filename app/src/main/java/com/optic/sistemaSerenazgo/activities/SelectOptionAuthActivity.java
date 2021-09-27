package com.optic.sistemaSerenazgo.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.activities.patrol.RegisterPatrolActivity;
import com.optic.sistemaSerenazgo.activities.serene.RegisterActivity;
import com.optic.sistemaSerenazgo.includes.MyToolbar;

public class SelectOptionAuthActivity extends AppCompatActivity {

    TextView mButtonGoToLogin;
    TextView mButtonGoToRegister;
    TextView mButtonLostPassword;
    TextView type;
    SharedPreferences mPref;
    ProgressBar mBar;
    ImageView mDelete;
    DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);
        MyToolbar.show(this, "Seleccione una opción", true);

        mReference = FirebaseDatabase.getInstance().getReference();
        mBar                = findViewById(R.id.bar);
        type                = findViewById(R.id.txt_type);
        mButtonGoToLogin    = findViewById(R.id.btnGoToLogin);
        mButtonGoToRegister = findViewById(R.id.btnGoToRegister);
        mButtonLostPassword = findViewById(R.id.btn_lost);
        mDelete             = findViewById(R.id.imv_delete);

        mButtonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });
        mButtonGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });
        mButtonLostPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLostPassword();
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SelectOptionAuthActivity.this);
                builder.setTitle("Alerta!");
                builder.setIcon(R.drawable.ic_delete_db);
                builder.setMessage("Está seguro de eliminar la BASE DE DATOS?");
                builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReference.child("Users").removeValue();
                        mReference.child("incidents").removeValue();
                        mReference.child("heatMap").removeValue();
                        mReference.child("Tokens").removeValue();
                        Toast.makeText(SelectOptionAuthActivity.this, "BASE DE DATOS ELIMINADA!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //VABLE DE CACHÉ
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        verifyUser();


    }


    public void verifyUser(){
        String typeUser = mPref.getString("user", "");
        if (typeUser.equals("client")){
            type.setText("Usuario : Sereno");
        } else {
            type.setText("Usuario : Patrulla");
        }
    }

    //IR AL LOGIN SI PRESIONAMOS SEGUN OPCION
    public void goToLogin() {
        disable();
        String typeUser = mPref.getString("user", "");
        Intent intent = new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        intent.putExtra("user",typeUser);
        startActivity(intent);
    }
    //REGISTRAR SEGUN LO PRESIONADO UTLIZANDO LA KEY ALMACENADA EN CACHÉ
    public void goToRegister() {
        disable();
        mButtonGoToRegister.setTextColor(Color.parseColor("#FC0000"));
        String typeUser = mPref.getString("user", "");
        if (typeUser.equals("client")) {
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
            intent.putExtra("user","client");
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterPatrolActivity.class);
            intent.putExtra("user","patrol");
            startActivity(intent);
        }
    }

    public void goToLostPassword(){
        disable();
        mButtonLostPassword.setTextColor(Color.parseColor("#FC0000"));
        Intent intent = new Intent(SelectOptionAuthActivity.this, ForgotPassword.class);
        startActivity(intent);
    }

    public void disable(){
        mBar.setVisibility(View.VISIBLE);
        mButtonGoToLogin.setEnabled(false);
        mButtonLostPassword.setEnabled(false);
        mButtonGoToRegister.setEnabled(false);
    }

    public void enable(){
        mBar.setVisibility(View.GONE);
        mButtonGoToLogin.setEnabled(true);
        mButtonLostPassword.setEnabled(true);
        mButtonGoToRegister.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enable();
        mButtonGoToRegister.setTextColor(Color.parseColor("#F1C40F"));
        mButtonLostPassword.setTextColor(Color.parseColor("#F1C40F"));
    }

    @Override
    public void onBackPressed() {
        if (mBar.getVisibility() == View.VISIBLE){
        }
        else{
            super.onBackPressed();
        }
    }
}
