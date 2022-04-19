package com.ksp.subitesv.proveedores;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ksp.subitesv.utils.CompressorBitmapImage;

import java.io.File;

public class ProveedorImagenes {

    private StorageReference mStorage;

    public ProveedorImagenes(String ref){
        mStorage = FirebaseStorage.getInstance().getReference().child(ref);
    }

    public UploadTask guardarImagen(Context context, File imagen, String idUsuario){
        byte[] imageByte = CompressorBitmapImage.getImage(context, imagen.getPath(), 500, 500);
        final StorageReference storage = mStorage.child(idUsuario + ".jpg");
        mStorage = storage;
        UploadTask uploadTask = storage.putBytes(imageByte);
        return uploadTask;
    }

    public StorageReference obtenerStorage(){
        return mStorage;

    }


}
