package com.optic.sistemaSerenazgo.activities.serene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.activities.DetailIndicent;
import com.optic.sistemaSerenazgo.activities.patrol.IncidentListPatrol;
import com.optic.sistemaSerenazgo.activities.patrol.MapPatrolActivity;
import com.optic.sistemaSerenazgo.adapters.AdapterListIncident;
import com.optic.sistemaSerenazgo.includes.MyToolbar;
import com.optic.sistemaSerenazgo.models.Incidents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class IncidentList extends AppCompatActivity {
    private ProgressDialog mDialogActualizeData;
    DatabaseReference mDatabaseReference;
    ArrayList<Incidents> listIncidents;
    RecyclerView recyclerViewIncident;
    SearchView searchView;
    AdapterListIncident adapterListIncident;
    LinearLayoutManager linearLayoutManager;
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeDark);
        setContentView(R.layout.activity_incident_list_serene);
        MyToolbar.show(this,"Lista de incidentes",true);
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        mDialogActualizeData = new ProgressDialog(this,R.style.MyAlertDialogData);
        mDialogActualizeData.setCancelable(false);
        mDialogActualizeData.show();
        mDialogActualizeData.setContentView(R.layout.dialog_data);
        mDialogActualizeData.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("incidents");
        recyclerViewIncident = findViewById(R.id.recyclerIncidents);
        searchView = findViewById(R.id.searchIncidents);
        searchView.setBackgroundColor(Color.WHITE);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewIncident.setLayoutManager(linearLayoutManager);
        listIncidents = new ArrayList<>();
        adapterListIncident = new AdapterListIncident(this,listIncidents);
        recyclerViewIncident.setAdapter(adapterListIncident);

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

                    Toast.makeText(IncidentList.this, "Sin registros", Toast.LENGTH_SHORT).show();
                    mDialogActualizeData.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(IncidentList.this, "Error de base de datos", Toast.LENGTH_SHORT).show();            }
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
        String typeUser = mPref.getString("user", "");
        if (typeUser.equals("client")){
            startActivity(new Intent(IncidentList.this, MapSereneActivity.class));
            finish();
        } else {
            startActivity(new Intent(IncidentList.this, MapPatrolActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}