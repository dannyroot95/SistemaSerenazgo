package com.optic.sistemaSerenazgo.activities.patrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.models.HistoryBooking;
import com.optic.sistemaSerenazgo.providers.SereneProvider;
import com.optic.sistemaSerenazgo.providers.HistoryBookingProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetailPatrolActivity extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewYourCalification;
    private RatingBar mRatingBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageBack;

    private String mExtraId;
    private HistoryBookingProvider mHistoryBookingProvider;
    private SereneProvider mClientProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_patrol);

        mTextViewName = findViewById(R.id.textViewNameBookingDetail);
        mTextViewOrigin = findViewById(R.id.textViewOriginHistoryBookingDetail);
        mTextViewDestination = findViewById(R.id.textViewDestinationHistoryBookingDetail);
        mTextViewYourCalification = findViewById(R.id.textViewCalificationHistoryBookingDetail);
        mRatingBarCalification = findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage = findViewById(R.id.circleImageHistoryBookingDetail);
        mCircleImageBack = findViewById(R.id.circleImageBack);

        mClientProvider = new SereneProvider();
        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider = new HistoryBookingProvider();
        getHistoryBooking();

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    
    // en esta Clase obtenemos es estado de la patrulla (si culminó un incidente o no)

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HistoryBooking historyBooking = dataSnapshot.getValue(HistoryBooking.class);
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewYourCalification.setText("" + historyBooking.getCalificationDriver());

                    if (dataSnapshot.hasChild("calificationClient")) {
                        mRatingBarCalification.setRating((float) historyBooking.getCalificationClient());
                    }

                    mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String name = dataSnapshot.child("name").getValue().toString();
                                mTextViewName.setText(name.toUpperCase());
                                if (dataSnapshot.hasChild("image")) {
                                    String image = dataSnapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailPatrolActivity.this).load(image).into(mCircleImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
