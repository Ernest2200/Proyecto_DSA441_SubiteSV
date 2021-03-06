package com.ksp.subitesv.actividades.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ksp.subitesv.R;
import com.ksp.subitesv.includes.AppToolBar;
import com.ksp.subitesv.modulos.Info;
import com.ksp.subitesv.proveedores.GoogleApiProveedor;
import com.ksp.subitesv.proveedores.ProveedorInfo;
import com.ksp.subitesv.utils.DecodificadorPuntos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallesSolicitudActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProveedor mGoogleApiProvider;
    private ProveedorInfo mProveedorInfo;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewTime;
    private TextView mTextViewPrice;

    private Button mButtonRequest;

    private String mExtraOrigin;
    private String mExtraDestination;

    private CircleImageView mCircleImageAtras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_solicitud);
       // AppToolBar.mostrar(this, "TUS DATOS", true);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLng);


        mGoogleApiProvider = new GoogleApiProveedor(DetallesSolicitudActivity.this);
        mProveedorInfo = new ProveedorInfo();

        mTextViewOrigin = findViewById(R.id.textViewOrigin);
        mTextViewDestination = findViewById(R.id.textViewDestination);
        mTextViewTime = findViewById(R.id.textViewTime);
        mTextViewPrice = findViewById(R.id.textViewPrice);
        mButtonRequest = findViewById(R.id.btnRequestNow);

        mTextViewOrigin.setText(mExtraOrigin);
        mTextViewDestination.setText(mExtraDestination);

        mCircleImageAtras=findViewById(R.id.imagenCicularAtras);

        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRequestDriver();
            }
        });

        mCircleImageAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void goToRequestDriver() {
        Intent intent = new Intent(DetallesSolicitudActivity.this, SolicitarConductorActivity.class);
        intent.putExtra("origin_lat", mOriginLatLng.latitude);
        intent.putExtra("origin_lng", mOriginLatLng.longitude);
        intent.putExtra("origin", mExtraOrigin);
        intent.putExtra("destination", mExtraDestination);
        intent.putExtra("destination_lat", mDestinationLatLng.latitude);
        intent.putExtra("destination_lng", mDestinationLatLng.longitude);
        startActivity(intent);
        finish();
    }

    private void drawRoute() {
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng).enqueue(new Callback<String>() {


            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodificadorPuntos.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(8f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray legs =  route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                    mTextViewTime.setText(durationText + " " + distanceText);

                    String[] distanciaYkm = distanceText.split(" ");
                    double valorDistancia = Double.parseDouble(distanciaYkm[0]) ;

                    String[] duracionYmins = durationText.split(" ");
                    double valorDuracion = Double.parseDouble(duracionYmins[0]) ;

                    calcularPrecio(valorDistancia, valorDuracion);

                } catch(Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void calcularPrecio(double valorDistancia, double valorDuracion) {//Funcion para calcular el precio del viaje
        mProveedorInfo.obtenerInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Info info = snapshot.getValue(Info.class);
                    double totalDistancia = valorDistancia * info.getKm();
                    double totalDuracion = valorDuracion * info.getMin();
                    double total = totalDistancia + totalDuracion;
                    double minTotal =  Math.round(total - 0.5);
                    double maxTotal =  Math.round(total + 0.5);
                    mTextViewPrice.setText(minTotal + " - " + maxTotal + " $");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mOriginLatLng)
                        .zoom(14f)
                        .build()
        ));

        drawRoute();
    }


}