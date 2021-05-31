package com.optic.sistemaSerenazgo.activities.serene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.sistemaSerenazgo.R;
import com.optic.sistemaSerenazgo.models.SereneBooking;
import com.optic.sistemaSerenazgo.models.FCMBody;
import com.optic.sistemaSerenazgo.models.FCMResponse;
import com.optic.sistemaSerenazgo.providers.AuthProvider;
import com.optic.sistemaSerenazgo.providers.SereneBookingProvider;
import com.optic.sistemaSerenazgo.providers.GeofireProvider;
import com.optic.sistemaSerenazgo.providers.GoogleApiProvider;
import com.optic.sistemaSerenazgo.providers.NotificationProvider;
import com.optic.sistemaSerenazgo.providers.TokenProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestPatrolActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;
    private GeofireProvider mGeofireProvider;

    private String mExtraOrigin;
    private String mExtraDestination;
    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private double mRadius = 0.1;
    private boolean mDriverFound = false;
    private String  mIdDriverFound = "";
    private LatLng mDriverFoundLatLng;
    private NotificationProvider mNotificationProvider;
    private TokenProvider mTokenProvider;
    private SereneBookingProvider mClientBookingProvider;
    private AuthProvider mAuthProvider;
    private GoogleApiProvider mGoogleApiProvider;

    private ValueEventListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_patrol);

        mAnimation = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);

        mAnimation.playAnimation();

        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);
        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng= new LatLng(mExtraDestinationLat, mExtraDestinationLng);

        mGeofireProvider = new GeofireProvider("active_drivers");
        mTokenProvider = new TokenProvider();
        mNotificationProvider = new NotificationProvider();
        mClientBookingProvider = new SereneBookingProvider();
        mAuthProvider = new AuthProvider();
        mGoogleApiProvider = new GoogleApiProvider(RequestPatrolActivity.this);
        mButtonCancelRequest.setVisibility(View.INVISIBLE);

        mButtonCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRequest();
            }
        });

        getClosestDriver();
    }

    private void cancelRequest() {

        mClientBookingProvider.delete(mAuthProvider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sendNotificationCancel();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(RequestPatrolActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getClosestDriver() {
        mGeofireProvider.getActiveDrivers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!mDriverFound) {
                    mButtonCancelRequest.setVisibility(View.VISIBLE);
                    mDriverFound = true;
                    mIdDriverFound = key;
                    mDriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                    mTextViewLookingFor.setText("PATRULLA ENCONTRADA\nESPERANDO RESPUESTA");
                    createClientBooking();
                    Log.d("DRIVER", "ID: " + mIdDriverFound);
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                // INGRESA CUANDO TERMINA LA BUSQUEDA DEL CONDUCTOR EN UN RADIO DE 0.1 KM
                if (!mDriverFound) {
                    mRadius = mRadius + 0.1f;
                    // NO ENCONTRO NINGUN CONDUCTOR
                    if (mRadius > 5) {
                        mTextViewLookingFor.setText("NO SE ENCONTRÓ UNA PATRULLA CERCANA");
                        Toasty.error(RequestPatrolActivity.this, "NO SE ENCONTRÓ UNA PATRULLA", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    else {
                        getClosestDriver();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void createClientBooking() {

        mGoogleApiProvider.getDirections(mOriginLatLng, mDriverFoundLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    JSONArray legs =  route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                    sendNotification(durationText, distanceText);

                } catch(Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });


    }

    private void sendNotificationCancel() {
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "SOLICITUD CANCELADA");
                    map.put("body",
                            "El usuario canceló la solicitud"
                    );
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1) {
                                    Toasty.error(RequestPatrolActivity.this, "La solicitud se cancelo correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RequestPatrolActivity.this, MapSereneActivity.class);
                                    startActivity(intent);
                                    finish();
                                    //Toast.makeText(RequestDriverActivity.this, "La notificacion se ha enviado correctamente", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toasty.error(RequestPatrolActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toasty.error(RequestPatrolActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error " + t.getMessage());
                        }
                    });
                }
                else {
                    Toasty.error(RequestPatrolActivity.this, "No se pudo enviar la notificacion porque la patrulla no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(final String time, final String km) {
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "SOLICITUD DE SERVICIO A " + time + " DE TU POSICION");
                    map.put("body",
                            "Un cliente esta solicitando un servicio a una distancia de " + km + "\n" +
                                    "Recoger en: " + mExtraOrigin + "\n" +
                                    "Destino: " + mExtraDestination
                    );
                    map.put("idClient", mAuthProvider.getId());
                    map.put("origin", mExtraOrigin);
                    map.put("destination", mExtraDestination);
                    map.put("min", time);
                    map.put("distance", km);
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1) {
                                    SereneBooking clientBooking = new SereneBooking(
                                            mAuthProvider.getId(),
                                            mIdDriverFound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng
                                    );

                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            checkStatusClientBooking();
                                        }
                                    });
                                    //Toast.makeText(RequestDriverActivity.this, "La notificacion se ha enviado correctamente", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toasty.error(RequestPatrolActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toasty.error(RequestPatrolActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error " + t.getMessage());
                        }
                    });
                }
                else {
                    Toasty.error(RequestPatrolActivity.this, "No se pudo enviar la notificacion porque el patrullero no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkStatusClientBooking() {
        mListener = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String status = dataSnapshot.getValue().toString();
                    if (status.equals("accept")) {
                        Intent intent = new Intent(RequestPatrolActivity.this, MapSereneBookingActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (status.equals("cancel")) {
                        Toasty.error(RequestPatrolActivity.this, "El patrullero no aceptó la solicitud", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RequestPatrolActivity.this, MapSereneActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
