package com.optic.sistemaSerenazgo.activities.patrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.includes.MyToolbar;
import com.optic.sistemaSerenazgo.models.Patrol;
import com.optic.sistemaSerenazgo.providers.AuthProvider;
import com.optic.sistemaSerenazgo.providers.PatrolProvider;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class RegisterPatrolActivity extends AppCompatActivity {

    AuthProvider mAuthProvider;
    PatrolProvider mDriverProvider;

    // VIEWS
    Button mButtonRegister;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputVehicleBrand;
    TextInputEditText mTextInputVehiclePlate;
    TextInputEditText mTextInputPassword;

    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_patrol);
        MyToolbar.show(this, "Registro de Patrullero", true);

        mAuthProvider = new AuthProvider();
        mDriverProvider = new PatrolProvider();

        mDialog = new SpotsDialog.Builder().setContext(RegisterPatrolActivity.this).setMessage("Espere un momento").build();

        mButtonRegister = findViewById(R.id.btnRegister);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputName = findViewById(R.id.textInputName);
        mTextInputVehicleBrand = findViewById(R.id.textInputVehicleBrand);
        mTextInputVehiclePlate = findViewById(R.id.textInputVehiclePlate);
        mTextInputPassword = findViewById(R.id.textInputPassword);

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegister();
            }
        });
    }

    //REGISTRAR EN LA BASE DE DATOS LOS DATOS DE LA PATRULLA

    void clickRegister() {
        final String name = mTextInputName.getText().toString();
        final String email = mTextInputEmail.getText().toString();
        final String vehicleBrand = mTextInputVehicleBrand.getText().toString();
        final String vehiclePlate = mTextInputVehiclePlate.getText().toString();
        final String password = mTextInputPassword.getText().toString();

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !vehicleBrand.isEmpty() && !vehiclePlate.isEmpty()) {
            if (password.length() >= 6) {
                mDialog.show();
                register(name, email, password, vehicleBrand, vehiclePlate);
            }
            else {
                Toasty.info(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toasty.info(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    //CREAMOS UN CONSTRUCTOR Y UTILIZAMOS EL MODELO PRA HACER EL REGISTRO

    void register(final String name, final String email, String password, final String vehicleBrand, final String vehiclePlate) {
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if (task.isSuccessful()) {
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Patrol driver = new Patrol(id, name, email, vehicleBrand, vehiclePlate);
                    create(driver);
                }
                else {
                    Toasty.error(RegisterPatrolActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //SI SE CREO SE EJECUTA EL ACTIVITY
    void create(Patrol driver) {
        mDriverProvider.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(RegisterPatrolActivity.this, MapPatrolActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    Toasty.error(RegisterPatrolActivity.this, "No se pudo crear el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
