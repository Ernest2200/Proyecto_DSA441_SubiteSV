package com.ksp.subitesv.actividades.conductor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ksp.subitesv.R;
import com.ksp.subitesv.actividades.cliente.ActualizarPerfilActivity;
import com.ksp.subitesv.includes.AppToolBar;
import com.ksp.subitesv.modulos.Cliente;
import com.ksp.subitesv.modulos.Conductor;
import com.ksp.subitesv.proveedores.AuthProveedores;
import com.ksp.subitesv.proveedores.ProveedorCliente;
import com.ksp.subitesv.proveedores.ProveedorConductor;
import com.ksp.subitesv.proveedores.ProveedorImagenes;
import com.ksp.subitesv.utils.CompressorBitmapImage;
import com.ksp.subitesv.utils.FileUtil;

import java.io.File;

public class ActualizarPerfilConductorActivity extends AppCompatActivity {


    private ImageView mImageViewPerfil;
    private Button mButtonActualizar;
    private TextView mTextViewNombre;
    private TextView mTextMarcaVehiculo;
    private TextView mTextViewPlacaVehiculo;

    private ProveedorConductor mProveedorConductor;
    private AuthProveedores mAuthProveedor;
    private ProveedorImagenes mProveedorImagenes;
    private File mImagenFile;
    private  String mImagen;
    private final int GALERIA_REQUEST = 1;
    private ProgressDialog mProgressDialog;
    private String mNombre;
    private String mMarcaVehiculo;
    private String mPlacaVehiculo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_perfil_conductor);
        AppToolBar.mostrar(this, "Actualizar Perfil", true);

        mImageViewPerfil = findViewById(R.id.imagenViewPerfil);
        mButtonActualizar = findViewById(R.id.btnActualizarPerfil);
        mTextViewNombre = findViewById(R.id.txtNombreRegistro);
        mTextMarcaVehiculo = findViewById(R.id.TxtMarcaVehiculo);
        mTextViewPlacaVehiculo =findViewById(R.id.TxtPlacaVehiculo);

        mProveedorConductor = new ProveedorConductor();
        mAuthProveedor = new AuthProveedores();
        mProgressDialog = new ProgressDialog(this);
        mProveedorImagenes = new ProveedorImagenes("driver_images");

        obtenerInformacionConductor();

        mImageViewPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria();


            }
        });


        mButtonActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actulizarPerfil();
            }
        });

    }

    private void abrirGaleria() {
        Intent galeriaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galeriaIntent.setType("image/*");
        startActivityForResult(galeriaIntent,GALERIA_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALERIA_REQUEST && resultCode == RESULT_OK) {
            try {
                mImagenFile = FileUtil.from(this, data.getData());
                mImageViewPerfil.setImageBitmap(BitmapFactory.decodeFile(mImagenFile.getAbsolutePath()));
            } catch(Exception e) {
                Log.d("ERROR", "Mensaje: " +e.getMessage());
            }
        }
    }

    private void obtenerInformacionConductor(){
        mProveedorConductor.obtenerConductor(mAuthProveedor.obetenerId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombre").getValue().toString();
                    String marcaVehiculo= snapshot.child("marcaVehiculo").getValue().toString();
                    String placaVehiculo = snapshot.child("placaVehiculo").getValue().toString();
                    mTextViewNombre.setText(nombre);
                    mTextMarcaVehiculo.setText(marcaVehiculo);
                    mTextViewPlacaVehiculo.setText(placaVehiculo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void actulizarPerfil() {
        mNombre = mTextViewNombre.getText().toString();
        mMarcaVehiculo = mTextMarcaVehiculo.getText().toString();
        mPlacaVehiculo = mTextViewPlacaVehiculo.getText().toString();

        if (!mNombre.equals("") && mImagenFile != null) {
            mProgressDialog.setMessage("Espere un momento...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            guardarImagen();
        }
        else{
            Toast.makeText(this, "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarImagen() {
       mProveedorImagenes.guardarImagen(ActualizarPerfilConductorActivity.this,mImagenFile,mAuthProveedor.obetenerId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mProveedorImagenes.obtenerStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Conductor conductor = new Conductor();
                            conductor.setImagen(image);
                            conductor.setNombre(mNombre);
                            conductor.setId(mAuthProveedor.obetenerId());
                            conductor.setMarcaVehiculo(mMarcaVehiculo);
                            conductor.setPlacaVehiculo(mPlacaVehiculo);
                            mProveedorConductor.actualizar(conductor).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(ActualizarPerfilConductorActivity.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else {
                    Toast.makeText(ActualizarPerfilConductorActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}