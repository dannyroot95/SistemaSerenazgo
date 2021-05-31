package com.optic.sistemaSerenazgo.activities.patrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.models.SereneBooking;
import com.optic.sistemaSerenazgo.models.HistoryBooking;
import com.optic.sistemaSerenazgo.providers.SereneBookingProvider;
import com.optic.sistemaSerenazgo.providers.HistoryBookingProvider;

import java.util.Date;

public class SuccessfullySereneActivity extends AppCompatActivity {

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private Button mButtonCalification;

    private SereneBookingProvider mClientBookingProvider;

    private String mExtraClientId;

    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;

    private float mCalification = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_serene);

        mTextViewOrigin = findViewById(R.id.textViewOriginCalification);
        mButtonCalification = findViewById(R.id.btnCalification);

        mClientBookingProvider = new SereneBookingProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();

        mExtraClientId = getIntent().getStringExtra("idClient");

        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(SuccessfullySereneActivity.this,MapPatrolActivity.class);
                startActivity(intent);
            }
        });

        getClientBooking();
    }

    //ACTUALIZAMOS ESTADO CUANDO SE COMPLETA LA RUTA

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    SereneBooking clientBooking = dataSnapshot.getValue(SereneBooking.class);
                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    mHistoryBooking = new HistoryBooking(
                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
