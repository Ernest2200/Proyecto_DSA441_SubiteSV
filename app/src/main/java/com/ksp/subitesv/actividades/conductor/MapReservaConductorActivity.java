package com.ksp.subitesv.actividades.conductor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ksp.subitesv.R;
import com.ksp.subitesv.actividades.cliente.DetallesSolicitudActivity;
import com.ksp.subitesv.actividades.cliente.SolicitarConductorActivity;
import com.ksp.subitesv.modulos.FCMCuerpo;
import com.ksp.subitesv.modulos.FCMRespuesta;
import com.ksp.subitesv.modulos.Info;
import com.ksp.subitesv.modulos.ReservaCliente;
import com.ksp.subitesv.proveedores.AuthProveedores;
import com.ksp.subitesv.proveedores.GoogleApiProveedor;
import com.ksp.subitesv.proveedores.NotificacionProveedor;
import com.ksp.subitesv.proveedores.ProveedorCliente;
import com.ksp.subitesv.proveedores.ProveedorGeoFire;
import com.ksp.subitesv.proveedores.ProveedorInfo;
import com.ksp.subitesv.proveedores.ReservaClienteProveedor;
import com.ksp.subitesv.proveedores.TokenProveedor;
import com.ksp.subitesv.utils.DecodificadorPuntos;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapReservaConductorActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private AuthProveedores mAuthProveedores;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private ProveedorGeoFire mProveedorGeofire;
    private TokenProveedor mTokenProveedor;
    private ReservaClienteProveedor mReservaClienteProveedor;
    private ProveedorCliente mProveedorCliente;
    private NotificacionProveedor mNotificacionProveedor;


    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;

    private LatLng mLatLngActual;

    private TextView mTextViewReservaCliente;
    private TextView mTextViewEmailReservaCliente;
    private TextView mTextViewOrigenReservaCliente;
    private TextView mTextViewDestinoReservaCliente;
    private  TextView mTextViewTiempo;
    private ImageView mImageViewReservaCliente;

    private String mExtraClienteId;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProveedor mGoogleApiProvider;
    private ProveedorInfo mProveedorInfo;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private  Info mInfo;

    private boolean mPrimeraVez = true;
    private boolean mEstaCercadelCliente=false;

    private Button mButtonIniciarReserva;
    private Button mButtonTerminarReserva;

    double mDistanciaEnMetros = 1;
    int mMinutos = 0;
    int mSegundos = 0;
    Boolean mSegundosHanTerminado = false;
    Boolean mInicioViaje = false;
    Handler  mHandler = new Handler();
    Location mUbicacionAnterior = new Location("");

    Runnable runnable = new Runnable() {//CREACION CRONOMETRO
        @Override
        public void run() {
            mSegundos ++;//Incrementar en uno en uno

            if(!mSegundosHanTerminado){
                mTextViewTiempo.setText(mSegundos + " Seg");
            }else{
                mTextViewTiempo.setText(mMinutos + " Min " + mSegundos);
            }

            if(mSegundos == 59){
                mSegundos = 0;
                mSegundosHanTerminado = true;
                mMinutos ++;
            }
            mHandler.postDelayed(runnable,1000);

        }
    };



    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mLatLngActual = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMarker != null) {
                        mMarker.remove();
                    }

                    if(mInicioViaje){
                        mDistanciaEnMetros = mDistanciaEnMetros + mUbicacionAnterior.distanceTo(location);
                        Log.d("DISTANCIA","Distancia recorrida: " +mDistanciaEnMetros);
                    }
                    mUbicacionAnterior = location;

                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_conductor))
                    );
                    //Obtener localizacion en tiempo real
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));
                    actualizarUbicacion();

                    if (mPrimeraVez) {
                        mPrimeraVez = false;
                        obtenerReservaCliente();
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_reserva_conductor);

        mAuthProveedores = new AuthProveedores();
        mProveedorGeofire = new ProveedorGeoFire("conductores_trabajando");
        mTokenProveedor = new TokenProveedor();
        mProveedorCliente = new ProveedorCliente();
        mReservaClienteProveedor = new ReservaClienteProveedor();
        mNotificacionProveedor = new NotificacionProveedor();
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mProveedorInfo = new ProveedorInfo();

        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mTextViewReservaCliente= findViewById(R.id.textViewReservaCliente);
        mTextViewEmailReservaCliente= findViewById(R.id.textViewEmailReservaCliente);
        mTextViewOrigenReservaCliente= findViewById(R.id.textViewOrigenReservaCliente);
        mTextViewDestinoReservaCliente = findViewById(R.id.textViewDestinoReservaCliente);
        mButtonIniciarReserva=findViewById(R.id.btnComezarReserva);
        mButtonTerminarReserva=findViewById(R.id.btnTerminarReserva);
        mImageViewReservaCliente = findViewById(R.id.imageViewReservaCliente);
        mTextViewTiempo = findViewById(R.id.textViewTiempo);

        //mButtonIniciarReserva.setEnabled(false);
        obtenerInfo();
        mExtraClienteId = getIntent().getStringExtra("clienteId");
        mGoogleApiProvider = new GoogleApiProveedor(MapReservaConductorActivity.this);

        obtenerCliente();

        mButtonIniciarReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEstaCercadelCliente){
                    comenzarReserva();
                }else {
                    Toast.makeText(MapReservaConductorActivity.this,R.string.PosicionLejos, Toast.LENGTH_SHORT).show();
                }
               
            }
        });

        mButtonTerminarReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               terminarReserva();
            }
        });
    }

    private void calcularViaje(){

        if(mMinutos == 0){
            mMinutos = 1;
        }
        double precioMin = mMinutos * mInfo.getMin();
        double precioKm = (mDistanciaEnMetros / 1000) * mInfo.getKm();

        Log.d("VALORES", "Min total: " + mMinutos);
        Log.d("VALORES", "KM total: " + (mDistanciaEnMetros/1000));


       final double total = precioMin + precioKm;
        mReservaClienteProveedor.actualizarPrecio(mExtraClienteId,total).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mReservaClienteProveedor.actualizarEstado(mExtraClienteId,"Finalizar");
                Intent intent = new Intent(MapReservaConductorActivity.this,CalificacionClienteActivity.class);
                intent.putExtra("clienteId",mExtraClienteId);
                intent.putExtra("precio", total);
                startActivity(intent);
                finish();
            }
        });

    }


    private void obtenerInfo() {//Funcion para calcular el precio del viaje
        mProveedorInfo.obtenerInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mInfo = snapshot.getValue(Info.class);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void terminarReserva(){
        mReservaClienteProveedor.actualizarIdHistorialReserva(mExtraClienteId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                enviarNotificacion("viaje finalizado");
                if (mFusedLocation != null){
                    mFusedLocation.removeLocationUpdates(mLocationCallback);
                }
                mProveedorGeofire.removerUbicacion(mAuthProveedores.obetenerId());
                if(mHandler != null){
                    mHandler.removeCallbacks(runnable);
                }

                calcularViaje();
            }
        });




    }

    private void comenzarReserva(){
        mReservaClienteProveedor.actualizarEstado(mExtraClienteId,"Comenzar");
        mButtonIniciarReserva.setVisibility(View.GONE);
        mButtonTerminarReserva.setVisibility(View.VISIBLE);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
        drawRoute(mDestinationLatLng);
        enviarNotificacion("viaje iniciado");
        mInicioViaje = true;//Para saber que el conductor ya inicio el viaje
        mHandler.postDelayed(runnable,1000);//Iniciar temporizador
    }

    private double obtenerDistanciaEntre(LatLng ClienteLatlng, LatLng ConductorLatlng){
        double distancia=0;
        Location UbicacionCliente = new Location("");
        Location UbicacionConductor = new Location("");

        UbicacionCliente.setLatitude(ClienteLatlng.latitude);
        UbicacionCliente.setLongitude(ClienteLatlng.longitude);

        UbicacionConductor.setLatitude(ConductorLatlng.latitude);
        UbicacionConductor.setLongitude(ConductorLatlng.longitude);
        distancia = UbicacionCliente.distanceTo(UbicacionConductor);
        return distancia;
    }

    private void obtenerReservaCliente(){
        mReservaClienteProveedor.obtenerReservaCliente(mExtraClienteId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String destino = snapshot.child("destino").getValue().toString();
                    String origen = snapshot.child("origen").getValue().toString();
                    double destinoLat = Double.parseDouble(snapshot.child("destinoLat").getValue().toString());
                    double destinoLng = Double.parseDouble(snapshot.child("destinoLng").getValue().toString());

                    double origenLat = Double.parseDouble(snapshot.child("origenLat").getValue().toString());
                    double origenLng = Double.parseDouble(snapshot.child("origenLng").getValue().toString());

                    mOriginLatLng = new LatLng(origenLat,origenLng);
                    mDestinationLatLng = new LatLng(destinoLat,destinoLng);
                    mTextViewDestinoReservaCliente.setText("Destino: " + destino);
                    mTextViewOrigenReservaCliente.setText("Recoger en: " + origen);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));


                    drawRoute(mOriginLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void drawRoute(LatLng latLng ) {
        mGoogleApiProvider.getDirections(mLatLngActual,latLng).enqueue(new Callback<String>() {


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


                } catch(Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void obtenerCliente(){
        mProveedorCliente.obtenerCLiente(mExtraClienteId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String correo = snapshot.child("correo").getValue().toString();
                    String nombre = snapshot.child("nombre").getValue().toString();
                    String imagen = "";
                    if(snapshot.hasChild("imagen")){
                        imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.with(MapReservaConductorActivity.this).load(imagen).into(mImageViewReservaCliente);
                    }
                    mTextViewReservaCliente.setText(nombre);
                    mTextViewEmailReservaCliente.setText(correo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void actualizarUbicacion() {
        if (mAuthProveedores.sesionExistente() && mLatLngActual != null) {
            mProveedorGeofire.guardarUbicacion(mAuthProveedores.obetenerId(), mLatLngActual);

            if (!mEstaCercadelCliente){

                if (mOriginLatLng != null && mLatLngActual != null){
                    double distancia = obtenerDistanciaEntre(mOriginLatLng, mLatLngActual);//distancia retornada en metros
                    if (distancia <= 200){
                        //mButtonIniciarReserva.setEnabled(true);
                        mEstaCercadelCliente = true;
                        Toast.makeText(this,R.string.PosicionCliente, Toast.LENGTH_SHORT).show();
                    }
                }

            }

        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        startLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActive()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    } else {
                        showAlertDialogGps();
                    }

                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActive()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        } else {
            showAlertDialogGps();
        }

    }

    private void showAlertDialogGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean gpsActive() {
        boolean isActive = false;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }


    private void disconnect() {

        if (mFusedLocation != null) {

            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProveedores.sesionExistente()) {
                mProveedorGeofire.removerUbicacion(mAuthProveedores.obetenerId());

            }
        } else {
            Toast.makeText(this, "No se puede desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActive()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                } else {
                    showAlertDialogGps();
                }
            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActive()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
                showAlertDialogGps();
            }
        }
    }


    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapReservaConductorActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapReservaConductorActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            }
        }
    }


    private void enviarNotificacion(final String estado) {
        mTokenProveedor.obtenerToken(mExtraClienteId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {//contiene la informacion que esta dentro del nodo del id del usuario
                if(snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title","ESTADO DE TU VIAJE");
                    map.put("body",
                            "Tu estado del viaje es: " + estado
                    );
                    FCMCuerpo fcmCuerpo = new FCMCuerpo(token, "high","4500s",map);
                    mNotificacionProveedor.enviarNotificacion(fcmCuerpo).enqueue(new Callback<FCMRespuesta>() {
                        @Override
                        public void onResponse(Call<FCMRespuesta> call, Response<FCMRespuesta> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() != 1){
                                    Toast.makeText(MapReservaConductorActivity.this, R.string.notificacionNOEnviada, Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(MapReservaConductorActivity.this, R.string.notificacionNOEnviada, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMRespuesta> call, Throwable t) {
                            Log.d("Error","Error" + t.getMessage());

                        }
                    });
                }
                else {
                    Toast.makeText(MapReservaConductorActivity.this, R.string.nosePudoenviarNotificacionConductorNotieneToken, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}