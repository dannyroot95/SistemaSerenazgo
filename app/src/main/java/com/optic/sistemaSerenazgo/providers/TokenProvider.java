package com.optic.sistemaSerenazgo.providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.optic.sistemaSerenazgo.models.Token;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class TokenProvider {

    DatabaseReference mDatabase;

    //CONSULTA HACIA EL NODO TOKEN
    public TokenProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    //METODO PARA GENERAR UN TOKEN SEGUN USUARIO
    public void create(final String idUser) {
        if (idUser == null) return;
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Token token = new Token(instanceIdResult.getToken());
                mDatabase.child(idUser).setValue(token);
            }
        });
    }

    //CONSULTA PARA OBTENER EL TOKEN SEGUN ID DE UN USUARIO (SERENO Ó PATRULLA)
    public DatabaseReference getToken(String idUser) {
        return mDatabase.child(idUser);
    }


}
