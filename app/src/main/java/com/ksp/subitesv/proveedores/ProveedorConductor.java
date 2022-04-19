package com.ksp.subitesv.proveedores;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ksp.subitesv.modulos.Cliente;
import com.ksp.subitesv.modulos.Conductor;

import java.util.HashMap;
import java.util.Map;

public class ProveedorConductor {

    DatabaseReference mBasedeDatos;

    public ProveedorConductor() {
        mBasedeDatos = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Conductores");
    }

    public Task<Void> crear(Conductor conductor) {
        return mBasedeDatos.child(conductor.getId()).setValue(conductor);
    }

    public DatabaseReference obtenerConductor(String conductorId){
        return mBasedeDatos.child(conductorId);
    }
    public Task<Void> actualizar(Conductor conductor){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", conductor.getNombre());  //marcaVehiculo,
        map.put("imagen",conductor.getImagen());
        map.put("marcaVehiculo",conductor.getMarcaVehiculo());
        map.put("placaVehiculo",conductor.getPlacaVehiculo());
        return  mBasedeDatos.child(conductor.getId()).updateChildren(map);
    }

}
