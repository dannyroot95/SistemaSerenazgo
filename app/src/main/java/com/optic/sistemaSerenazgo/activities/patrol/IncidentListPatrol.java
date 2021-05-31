package com.optic.sistemaSerenazgo.activities.patrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.adapters.AdapterListIncident;
import com.optic.sistemaSerenazgo.includes.MyToolbar;
import com.optic.sistemaSerenazgo.models.Incidents;

import java.util.ArrayList;
import java.util.Collections;

public class IncidentListPatrol extends AppCompatActivity {

    private ProgressDialog mDialogActualizeData;
    private FirebaseAuth mAuth;
    DatabaseReference mDatabaseReference;
    ArrayList<Incidents> listIncidents;
    RecyclerView recyclerViewIncident;
    SearchView searchView;
    AdapterListIncident adapterListIncident;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeDark);
        setContentView(R.layout.activity_incident_list_patrol);
        MyToolbar.show(this,"Lista de incidentes",true);
        mAuth = FirebaseAuth.getInstance();
        mDialogActualizeData = new ProgressDialog(this,R.style.MyAlertDialogData);
        mDialogActualizeData.setCancelable(false);
        mDialogActualizeData.show();
        mDialogActualizeData.setContentView(R.layout.dialog_data);
        mDialogActualizeData.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        String id = mAuth.getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(id).child("incidents");
        recyclerViewIncident = findViewById(R.id.recyclerIncidents);
        searchView = findViewById(R.id.searchIncidents);
        searchView.setBackgroundColor(Color.WHITE);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewIncident.setLayoutManager(linearLayoutManager);
        listIncidents = new ArrayList<>();
        adapterListIncident = new AdapterListIncident(this,listIncidents);
        recyclerViewIncident.setAdapter(adapterListIncident);

        // OBTENEMOS TODOS LOS REGISTROS DE INCIDENTES DE LA BASE DE DATOS E INFLAMOS EN EL RECYCLERVIEW

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    listIncidents.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Incidents pd = snapshot.getValue(Incidents.class);
                        listIncidents.add(pd);
                    }

                    Collections.reverse(listIncidents);
                    adapterListIncident.notifyDataSetChanged();
                    mDialogActualizeData.dismiss();
                }
                else {

                    Toast.makeText(IncidentListPatrol.this, "Sin registros", Toast.LENGTH_SHORT).show();
                    mDialogActualizeData.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(IncidentListPatrol.this, "Error de base de datos", Toast.LENGTH_SHORT).show();            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String texto) {
                buscar(texto);
                return true;
            }
        });

    }

    // ALGORITMO DE BUSQUEDA SEGÚN NUMERO DE INCIDENTE Ó FECHA

    private void buscar(String texto) {
        ArrayList<Incidents> lista = new ArrayList<>();
        for(Incidents object : listIncidents){
            if (object.getNumFicha().toLowerCase().contains(texto.toLowerCase()) ||
                    object.getFecha().toLowerCase().contains(texto.toLowerCase()))
            {
                lista.add(object);
            }
            else {
            }
        }
        AdapterListIncident adapter = new AdapterListIncident(this,lista);
        recyclerViewIncident.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}