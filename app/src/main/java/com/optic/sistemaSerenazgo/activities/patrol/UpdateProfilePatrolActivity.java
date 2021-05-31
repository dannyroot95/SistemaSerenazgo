package com.optic.sistemaSerenazgo.activities.patrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.models.Patrol;
import com.optic.sistemaSerenazgo.providers.AuthProvider;
import com.optic.sistemaSerenazgo.providers.PatrolProvider;
import com.optic.sistemaSerenazgo.providers.ImagesProvider;
import com.optic.sistemaSerenazgo.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;

import es.dmoral.toasty.Toasty;

public class UpdateProfilePatrolActivity extends AppCompatActivity {

    private ImageView mImageViewProfile;
    private Button mButtonUpdate;
    private TextView mTextViewName;
    private TextView mTextViewBrandVehicle;
    private TextView mTextViewPlateVehicle;
    private PatrolProvider mDriverProvider;
    private AuthProvider mAuthProvider;
    private ImagesProvider mImageProvider;

    private File mImageFile;
    private String mImage;

    private final int GALLERY_REQUEST = 1;
    private ProgressDialog mProgressDialog;
    private String mName;
    private String mVehicleBrand;
    private String mVehiclePlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_patrol);
        //MyToolbar.show(this, "Actualizar perfil", true);

        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mButtonUpdate = findViewById(R.id.btnUpdateProfile);
        mTextViewName = findViewById(R.id.textInputName);
        mTextViewBrandVehicle = findViewById(R.id.textInputVehicleBrand);
        mTextViewPlateVehicle = findViewById(R.id.textInputVehiclePlate);

        mDriverProvider = new PatrolProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImagesProvider("driver_images");

        mProgressDialog = new ProgressDialog(this);

        getDriverInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

    }

    //ABRIMOS LA GALERIA DE NUESTRO TELÉFONO

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST );
    }

    //UTILIZAMOS LA RUTA PARA ENCONTRAR LAS IMAGENE CON EL RESULTACTIVITY
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                assert data != null;
                mImageFile = FileUtil.from(this, data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch(Exception e) {
                Log.d("ERROR", "Mensaje: " +e.getMessage());
            }
        }
    }

    //OBTENEMOS LOS DATOS DEL LA PATRULLA
    private void getDriverInfo() {
        mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String vehicleBrand = dataSnapshot.child("vehicleBrand").getValue().toString();
                    String vehiclePlate = dataSnapshot.child("vehiclePlate").getValue().toString();
                    String image = "";
                    if (dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfilePatrolActivity.this).load(image).into(mImageViewProfile);
                    }
                    mTextViewName.setText(name);
                    mTextViewBrandVehicle.setText(vehicleBrand);
                    mTextViewPlateVehicle.setText(vehiclePlate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //ACTUALIZAMOS LOS DATOS
    private void updateProfile() {
        mName = mTextViewName.getText().toString();
        mVehicleBrand = mTextViewBrandVehicle.getText().toString();
        mVehiclePlate = mTextViewPlateVehicle.getText().toString();
        if (!mName.equals("") && mImageFile != null) {
            mProgressDialog.setMessage("Espere un momento...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            saveImage();
        }
        else {
            Toasty.info(this, "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }

    //GUADAMOS LA IMAGEN Y TAMBIEN LO ENVIAMOS A LA BASE DE DATOS (FIREBASE STORAGE)
    private void saveImage() {
        mImageProvider.saveImage(UpdateProfilePatrolActivity.this, mImageFile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Patrol driver = new Patrol();
                            driver.setImage(image);
                            driver.setName(mName);
                            driver.setId(mAuthProvider.getId());
                            driver.setVehicleBrand(mVehicleBrand);
                            driver.setVehiclePlate(mVehiclePlate);
                            mDriverProvider.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    finish();
                                    Toast.makeText(UpdateProfilePatrolActivity.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else {
                    Toasty.error(UpdateProfilePatrolActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
