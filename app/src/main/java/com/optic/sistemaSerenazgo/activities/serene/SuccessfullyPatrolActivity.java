package com.optic.sistemaSerenazgo.activities.serene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.models.SereneBooking;
import com.optic.sistemaSerenazgo.models.HistoryBooking;
import com.optic.sistemaSerenazgo.providers.AuthProvider;
import com.optic.sistemaSerenazgo.providers.SereneBookingProvider;
import com.optic.sistemaSerenazgo.providers.HistoryBookingProvider;

public class SuccessfullyPatrolActivity extends AppCompatActivity {

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private Button mButtonCalification;

    private SereneBookingProvider mClientBookingProvider;
    private AuthProvider mAuthProvider;

    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;

    private float mCalification = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_patrol);

        mTextViewOrigin = findViewById(R.id.textViewOriginCalification);
        mButtonCalification = findViewById(R.id.btnCalification);

        mClientBookingProvider = new SereneBookingProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();
        mAuthProvider = new AuthProvider();

        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getClientBooking();
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
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