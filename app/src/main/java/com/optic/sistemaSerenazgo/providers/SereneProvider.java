package com.optic.sistemaSerenazgo.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.sistemaSerenazgo.models.Serene;

import java.util.HashMap;
import java.util.Map;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class SereneProvider {

    DatabaseReference mDatabase;

    //CONSULTA HACIA EL NODO DE CLIENTS
    public SereneProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Clients");
    }

    //TAREA PARA CREAR UN USUARIO SERENO
    public Task<Void> create(Serene client) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", client.getName());
        map.put("email", client.getEmail());
        return mDatabase.child(client.getId()).setValue(map);
    }

    //TAREA PARA ACTUALIZAR UN USUARIO SERENO
    public Task<Void> update(Serene client) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", client.getName());
        map.put("image", client.getImage());
        return mDatabase.child(client.getId()).updateChildren(map);
    }

    //CONSULTA PARA OBTENER UN ID DEL SERENO
    public DatabaseReference getClient(String idClient) {
        return mDatabase.child(idClient);
    }

}
