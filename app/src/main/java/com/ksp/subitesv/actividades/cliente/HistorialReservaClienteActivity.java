package com.ksp.subitesv.actividades.cliente;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ksp.subitesv.R;
import com.ksp.subitesv.adapters.AdaptadorHistorialReservaCliente;
import com.ksp.subitesv.includes.AppToolBar;
import com.ksp.subitesv.modulos.HistorialReserva;
import com.ksp.subitesv.proveedores.AuthProveedores;

import android.os.Bundle;

public class HistorialReservaClienteActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private AdaptadorHistorialReservaCliente mAdapter;
    private AuthProveedores mAuthProveedores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_reserva_cliente);
        AppToolBar.mostrar(this,"Historial de Viajes", true);

        mRecyclerView = findViewById(R.id.RecyclerHistorialReservas);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthProveedores = new AuthProveedores();

        Query query = FirebaseDatabase.getInstance().getReference().child("HistorialReserva").orderByChild("idCliente").equalTo(mAuthProveedores.obetenerId());
        FirebaseRecyclerOptions<HistorialReserva> opciones = new FirebaseRecyclerOptions.Builder<HistorialReserva>()
                .setQuery(query, HistorialReserva.class).build();
        mAdapter = new AdaptadorHistorialReservaCliente(opciones, HistorialReservaClienteActivity.this);

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mAdapter.stopListening();
    }
}