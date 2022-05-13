package com.ksp.subitesv.proveedores;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProveedorInfo {

    DatabaseReference mBasedeDatos;

    public ProveedorInfo() {

        mBasedeDatos = FirebaseDatabase.getInstance().getReference().child("Info");
    }

    public  DatabaseReference obtenerInfo(){
         return  mBasedeDatos;
    }

}
