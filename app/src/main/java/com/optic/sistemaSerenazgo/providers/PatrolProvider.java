package com.optic.sistemaSerenazgo.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.sistemaSerenazgo.models.Patrol;

import java.util.HashMap;
import java.util.Map;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class PatrolProvider {

    DatabaseReference mDatabase;

    //PARA ENTRAR AL NODO DE LAS PATRULLAS EN LA BASE DE DATOS
    public PatrolProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
    }

    //PARA CREAR UNA TAREA DE ESTADO DE UBICACION DEL LA PATRULLA
    public Task<Void> create(Patrol driver) {
        return mDatabase.child(driver.getId()).setValue(driver);
    }

    //PARA OBTENER EL ID DEL PATRULLERO
    public DatabaseReference getDriver(String idDriver) {
        return mDatabase.child(idDriver);
    }

    //TAREA PARA REGISTRAR DATOS DEL PATRULLERO
    public Task<Void> update(Patrol driver) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", driver.getName());
        map.put("image", driver.getImage());
        map.put("vehicleBrand", driver.getVehicleBrand());
        map.put("vehiclePlate", driver.getVehiclePlate());
        return mDatabase.child(driver.getId()).updateChildren(map);
    }

}
