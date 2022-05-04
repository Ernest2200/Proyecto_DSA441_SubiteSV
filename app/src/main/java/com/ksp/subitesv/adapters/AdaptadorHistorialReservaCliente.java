package com.ksp.subitesv.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ksp.subitesv.R;
import com.ksp.subitesv.actividades.cliente.DetalleHistorialReservaClienteActivity;
import com.ksp.subitesv.actividades.cliente.HistorialReservaClienteActivity;
import com.ksp.subitesv.modulos.HistorialReserva;
import com.ksp.subitesv.proveedores.ProveedorConductor;
import com.squareup.picasso.Picasso;


public class AdaptadorHistorialReservaCliente extends FirebaseRecyclerAdapter<HistorialReserva, AdaptadorHistorialReservaCliente.ViewHolder> {

    private ProveedorConductor mProveedorCondutor;
    private Context mContext;

    public AdaptadorHistorialReservaCliente(FirebaseRecyclerOptions<HistorialReserva>opciones, Context context){
        super(opciones);
        mProveedorCondutor = new ProveedorConductor();
        mContext = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull HistorialReserva historialReserva) {

        final String id = getRef(position).getKey();

        holder.txtViewOrigen.setText(historialReserva.getOrigen());
        holder.txtViewDestino.setText(historialReserva.getDestino());
        holder.txtViewCalificacion.setText(String.valueOf(historialReserva.getCalificacionCliente()));
        mProveedorCondutor.obtenerConductor(historialReserva.getIdConductor()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    holder.txtViewNombre.setText(nombre);

                    if (snapshot.hasChild("imagen")){
                        String imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.with(mContext).load(imagen).into(holder.ImgViewHistorialReserva);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.mview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, DetalleHistorialReservaClienteActivity.class);
                intent.putExtra("idHistorialReserva", id);
                mContext.startActivity(intent);
            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_historial_reserva,parent,false);

        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView txtViewNombre;
        private TextView txtViewOrigen;
        private TextView txtViewDestino;
        private TextView txtViewCalificacion;
        private ImageView ImgViewHistorialReserva;
        private View mview;


        public ViewHolder(View view){
            super(view);

            mview = view;
            txtViewNombre=view.findViewById(R.id.txtViewNombre);
            txtViewDestino=view.findViewById(R.id.txtViewDestino);
            txtViewOrigen=view.findViewById(R.id.txtViewOrigen);
            txtViewCalificacion=view.findViewById(R.id.txtViewCalifacion);
            ImgViewHistorialReserva=view.findViewById(R.id.imgViewHistorialReseva);
        }
    }
}

