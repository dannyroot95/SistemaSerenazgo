package com.optic.sistemaSerenazgo.providers;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.optic.sistemaSerenazgo.utils.CompressorBitmapImage;

import java.io.File;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class ImagesProvider {

    private StorageReference mStorage;

    //PARA OBTENER LA IMAGEN DE LOS USUARIOS
    public ImagesProvider(String ref) {
        mStorage = FirebaseStorage.getInstance().getReference().child(ref);
    }

    //PARA CREAR UNA TAREA DE COMPRESION DE IMAGEN
    public UploadTask saveImage(Context context, File image, String idUser) {
        byte[] imageByte = CompressorBitmapImage.getImage(context, image.getPath(), 500, 500);
        final StorageReference storage = mStorage.child(idUser + ".jpg");
        mStorage = storage;
        UploadTask uploadTask = storage.putBytes(imageByte);
        return uploadTask;
    }

    public StorageReference getStorage() {
        return mStorage;
    }
}
