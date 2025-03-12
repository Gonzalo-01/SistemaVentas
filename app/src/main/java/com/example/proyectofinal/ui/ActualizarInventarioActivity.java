package com.example.proyectofinal.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.proyectofinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActualizarInventarioActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 101;
    private EditText editTextNombreProducto, editTextDescripcionProducto, editTextPrecio, editTextStock;
    private Button btnEscanearCodigo, btnTomarFoto;
    private ImageView imageViewProduct;
    private TextView textViewCodigoDeBarras;
    private Toolbar toolbar;
    private ListView lvProductos;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> productosList;
    private String codigoDeBarras, nombreProducto, descripcionProducto, precioProducto, stockProducto;
    private Uri photoUri;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_inventario);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        toolbar = findViewById(R.id.Ttoolbar);
        setSupportActionBar(toolbar);

        editTextNombreProducto = findViewById(R.id.editTextNombreProducto);
        editTextDescripcionProducto = findViewById(R.id.editTextDescripcionProducto);
        editTextPrecio = findViewById(R.id.editTextPrecio);
        editTextStock = findViewById(R.id.editTextStock);
        textViewCodigoDeBarras = findViewById(R.id.textViewCodigoDeBarras);

        btnEscanearCodigo = findViewById(R.id.btnEscanearCodigo);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        imageViewProduct = findViewById(R.id.imageViewProduct);

        lvProductos = findViewById(R.id.lv_productos);

        productosList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productosList);
        lvProductos.setAdapter(adapter);

        btnEscanearCodigo.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES); // Aquí configuras los formatos
            integrator.setPrompt("Escanear Código de Barras");
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan(); // Inicia el escaneo
        });

        btnTomarFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        obtenerProductos();

        if (savedInstanceState != null) {
            codigoDeBarras = savedInstanceState.getString("codigoDeBarras", "");
            textViewCodigoDeBarras.setText("Código de Barras: " + codigoDeBarras);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                codigoDeBarras = result.getContents();
                textViewCodigoDeBarras.setText("Código de Barras: " + codigoDeBarras);
                Toast.makeText(this, "Código de Barras: " + codigoDeBarras, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageViewProduct.setImageBitmap(imageBitmap);
            imageViewProduct.setVisibility(View.VISIBLE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataBAOS = baos.toByteArray();

            StorageReference filePath = mStorage.child("Photos").child(codigoDeBarras);
            UploadTask uploadTask = filePath.putBytes(dataBAOS);
            uploadTask.addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                photoUri = uri;
                Toast.makeText(ActualizarInventarioActivity.this, "Foto subida exitosamente", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> Toast.makeText(ActualizarInventarioActivity.this, "Error al subir la foto", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.icon_add) {
            nombreProducto = editTextNombreProducto.getText().toString();
            descripcionProducto = editTextDescripcionProducto.getText().toString();
            precioProducto = editTextPrecio.getText().toString();
            stockProducto = editTextStock.getText().toString();

            if (validarDatos(codigoDeBarras, nombreProducto, descripcionProducto, precioProducto, stockProducto)) {
                registrarProducto();
            }
            return true;
        } else if (id == R.id.icon_update) {
            nombreProducto = editTextNombreProducto.getText().toString();
            descripcionProducto = editTextDescripcionProducto.getText().toString();
            precioProducto = editTextPrecio.getText().toString();
            stockProducto = editTextStock.getText().toString();

            if (validarDatos(codigoDeBarras, nombreProducto, descripcionProducto, precioProducto, stockProducto)) {
                actualizarProducto(codigoDeBarras, nombreProducto, descripcionProducto, precioProducto, stockProducto);
            }
            return true;
        } else if (id == R.id.icon_delete) {
            if (!TextUtils.isEmpty(codigoDeBarras)) {
                eliminarProducto(codigoDeBarras);
            } else {
                Toast.makeText(ActualizarInventarioActivity.this, "Debe escanear un código de barras", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validarDatos(String codigoDeBarras, String nombreProducto, String descripcionProducto, String precioProducto, String stockProducto) {
        if (TextUtils.isEmpty(codigoDeBarras) || TextUtils.isEmpty(nombreProducto) || TextUtils.isEmpty(descripcionProducto) || TextUtils.isEmpty(precioProducto) || TextUtils.isEmpty(stockProducto)) {
            Toast.makeText(ActualizarInventarioActivity.this, "Debe completar los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(precioProducto);
        } catch (NumberFormatException e) {
            Toast.makeText(ActualizarInventarioActivity.this, "El precio debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Integer.parseInt(stockProducto);
        } catch (NumberFormatException e) {
            Toast.makeText(ActualizarInventarioActivity.this, "El stock debe ser un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registrarProducto() {
        Map<String, Object> map = new HashMap<>();
        map.put("codigoDeBarras", codigoDeBarras);
        map.put("nombre", nombreProducto);
        map.put("descripcion", descripcionProducto);
        map.put("precio", Double.parseDouble(precioProducto));  // Convertir a double
        map.put("stock", Integer.parseInt(stockProducto));      // Convertir a int
        map.put("fotoUri", photoUri != null ? photoUri.toString() : "");

        Log.d("REGISTRAR_PRODUCTO", "Datos del producto: " + map.toString());

        mDatabase.child("Productos").child(codigoDeBarras).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("REGISTRAR_PRODUCTO", "Producto registrado con éxito");
                Toast.makeText(ActualizarInventarioActivity.this, "Producto registrado con éxito", Toast.LENGTH_SHORT).show();
                obtenerProductos();  // Actualizar la lista de productos
            } else {
                Log.e("REGISTRAR_PRODUCTO", "Error al registrar el producto", task.getException());
                Toast.makeText(ActualizarInventarioActivity.this, "No se pudieron crear los datos correctamente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obtenerProductos() {
        mDatabase.child("Productos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productosList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String producto = ds.child("nombre").getValue(String.class);
                    productosList.add(producto);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OBTENER_PRODUCTOS", "Error al obtener productos", error.toException());
                Toast.makeText(ActualizarInventarioActivity.this, "Error al obtener productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarProducto(String codigoDeBarras, String nombreProducto, String descripcionProducto, String precioProducto, String stockProducto) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombreProducto);
        map.put("descripcion", descripcionProducto);
        map.put("precio", Double.parseDouble(precioProducto));  // Convertir a double
        map.put("stock", Integer.parseInt(stockProducto));      // Convertir a int
        map.put("fotoUri", photoUri != null ? photoUri.toString() : "");

        Log.d("ACTUALIZAR_PRODUCTO", "Datos del producto: " + map.toString());

        mDatabase.child("Productos").child(codigoDeBarras).updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("ACTUALIZAR_PRODUCTO", "Producto actualizado con éxito");
                Toast.makeText(ActualizarInventarioActivity.this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show();
                obtenerProductos();  // Actualizar la lista de productos
            } else {
                Log.e("ACTUALIZAR_PRODUCTO", "Error al actualizar el producto", task.getException());
                Toast.makeText(ActualizarInventarioActivity.this, "No se pudieron actualizar los datos correctamente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarProducto(String codigoDeBarras) {
        Log.d("ELIMINAR_PRODUCTO", "Código de barras del producto a eliminar: " + codigoDeBarras);

        mDatabase.child("Productos").child(codigoDeBarras).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("ELIMINAR_PRODUCTO", "Producto eliminado con éxito");
                Toast.makeText(ActualizarInventarioActivity.this, "Producto eliminado con éxito", Toast.LENGTH_SHORT).show();
                obtenerProductos();  // Actualizar la lista de productos
            } else {
                Log.e("ELIMINAR_PRODUCTO", "Error al eliminar el producto", task.getException());
                Toast.makeText(ActualizarInventarioActivity.this, "No se pudo eliminar este producto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("codigoDeBarras", codigoDeBarras);
    }
}
