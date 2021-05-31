package com.optic.sistemaSerenazgo.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.sistemaSerenazgo.models.Serene;
import com.optic.sistemaSerenazgo.models.SereneBooking;

import java.util.HashMap;
import java.util.Map;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class SereneBookingProvider {

    private DatabaseReference mDatabase;

    //PARA OBTENER ESTADO DEL SERENO
    public SereneBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    //PARA OBTENER EL ID DEL SERENO
    public Task<Void> create(SereneBooking clientBooking) {
        return mDatabase.child(clientBooking.getIdClient()).setValue(clientBooking);
    }

    //TAREA PARA ACTUALIZAR ESTADO DEL SERENO
    public Task<Void> updateStatus(String idClientBooking, String status) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    //TAREA PARA OBTENER EL ID DEL ESTADO DEL SERENO
    public Task<Void> updateIdHistoryBooking(String idClientBooking) {
        String idPush = mDatabase.push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("idHistoryBooking", idPush);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    //CONSULTA PARA OBTENER EL ID DEL SERENO
    public DatabaseReference getStatus(String idClientBooking) {
        return mDatabase.child(idClientBooking).child("status");
    }

    //CONSULTA PARA OBTENER EL ID DEL ESTADO DEL SERENO
    public DatabaseReference getClientBooking(String idClientBooking) {
        return mDatabase.child(idClientBooking);
    }

    //TAREA PARA BORRAR EL ESTADO DEL SERENO
    public Task<Void> delete(String idClientBooking) {
        return mDatabase.child(idClientBooking).removeValue();
    }
}
