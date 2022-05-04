package com.ksp.subitesv.actividades.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ksp.subitesv.R;
import com.ksp.subitesv.modulos.HistorialReserva;
import com.ksp.subitesv.proveedores.ProveedorConductor;
import com.ksp.subitesv.proveedores.ProveedorHistorialReserva;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetalleHistorialReservaClienteActivity extends AppCompatActivity {

    private TextView mTextViewNombre;
    private TextView mTextViewOrigen;
    private TextView mTextViewDestino;
    private TextView mTextViewTuCalificacion;
    private RatingBar mRatingBarCalifacion;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageAtras;

    private String mExtraId;

    private ProveedorConductor mProveedorConductor;
    private ProveedorHistorialReserva mHisotrialReservaProveedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_historial_reserva_cliente);

        mTextViewNombre=findViewById(R.id.txtViewNombreDetallesReserva);
        mTextViewOrigen=findViewById(R.id.txtViewOrigenHistorialDetalle);
        mTextViewDestino=findViewById(R.id.txtViewDestinoHistorialDetalle);
        mTextViewTuCalificacion=findViewById(R.id.txtViewCalificacionHistorialDetalle);
        mRatingBarCalifacion=findViewById(R.id.ratingBarHistorialReservaDetalle);
        mCircleImage=findViewById(R.id.imagenCicularDetalleHistorial);
        mCircleImageAtras=findViewById(R.id.imagenCicularAtras);

        mExtraId = getIntent().getStringExtra("idHistorialReserva");
        mHisotrialReservaProveedor = new ProveedorHistorialReserva();
        mProveedorConductor = new ProveedorConductor();
        obtenerHistorialReserva();

        mCircleImageAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void obtenerHistorialReserva(){
        mHisotrialReservaProveedor.obtenerHistorialReserva(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    HistorialReserva historialReserva = snapshot.getValue(HistorialReserva.class);

                    mTextViewOrigen.setText(historialReserva.getOrigen());
                    mTextViewDestino.setText(historialReserva.getDestino());
                    mTextViewTuCalificacion.setText("Tu Calificacion: " + historialReserva.getCalificacionConductor());

                    if (snapshot.hasChild("calificacionCliente")){
                        mRatingBarCalifacion.setRating((float) historialReserva.getCalificacionCliente());
                    }


                    mProveedorConductor.obtenerConductor(historialReserva.getIdConductor()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String nombre = snapshot.child("nombre").getValue().toString();
                                mTextViewNombre.setText(nombre.toUpperCase());

                                if (snapshot.hasChild("imagen")){
                                    String imagen = snapshot.child("imagen").getValue().toString();
                                    Picasso.with(DetalleHistorialReservaClienteActivity.this).load(imagen).into(mCircleImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}