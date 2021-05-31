package com.optic.sistemaSerenazgo.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.sistemaSerenazgo.models.HistoryBooking;

import java.util.HashMap;
import java.util.Map;

//CREAMOS UN PROVIDER PARA UTILIZAR MULTIPLES METODOS Y OPERACIONES

public class HistoryBookingProvider {

    private DatabaseReference mDatabase;

    //PARA OBTENER EL HISTORIAL DEL ESTADO
    public HistoryBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("HistoryBooking");
    }

    //PARA CREAR UNA TAREA DE ESTADOS
    public Task<Void> create(HistoryBooking historyBooking) {
        return mDatabase.child(historyBooking.getIdHistoryBooking()).setValue(historyBooking);
    }

    //PARA OBTENER EL ID DEL ESTADO DE LA RUTA
    public DatabaseReference getHistoryBooking(String idHistoryBooking) {
        return mDatabase.child(idHistoryBooking);
    }

}
