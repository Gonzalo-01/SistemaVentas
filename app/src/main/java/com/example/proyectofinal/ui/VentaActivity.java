package com.example.proyectofinal.ui;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectofinal.adaptador.ProductoAdapter;
import com.example.proyectofinal.R;
import com.example.proyectofinal.modelo.Producto;
import com.example.proyectofinal.modelo.Venta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class VentaActivity extends AppCompatActivity {

    private static final String TAG = "REGISTRAR_VENTA";
    private static final int CAMERA_REQUEST_CODE = 101;

    private Button btnEscanearCodigo, btnReporte, btnVender;
    private TextView textViewTotal;
    private RecyclerView recyclerView;
    private DatabaseReference mDatabase;
    private String codigoDeBarras;
    private Producto productoActual;

    private List<Producto> listaProductos;
    private ProductoAdapter adapter;
    private double totalVenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_venta);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        btnEscanearCodigo = findViewById(R.id.btnEscanearCodigo1);
        btnReporte = findViewById(R.id.btnReporte);
        btnVender = findViewById(R.id.btnVender);
        recyclerView = findViewById(R.id.recyclerView);
        textViewTotal = findViewById(R.id.textViewTotal);

        listaProductos = new ArrayList<>();
        adapter = new ProductoAdapter(listaProductos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnEscanearCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                } else {
                    iniciarEscaneo();
                }
            }
        });

        btnVender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarVenta();
            }
        });

        btnReporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearPdf();
            }
        });
    }

    private void iniciarEscaneo() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Escanear Código de Barras");
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarEscaneo();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                codigoDeBarras = result.getContents();
                Log.d(TAG, "Código de Barras escaneado: " + codigoDeBarras);
                buscarProducto(codigoDeBarras);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void buscarProducto(String codigoDeBarras) {
        Log.d(TAG, "Buscando producto con código: " + codigoDeBarras);
        mDatabase.child("Productos").child(codigoDeBarras).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        productoActual = snapshot.getValue(Producto.class);
                        if (productoActual != null) {
                            Log.d(TAG, "Producto encontrado: " + productoActual.toString());
                            agregarProductoALaLista();
                        } else {
                            Log.e(TAG, "Error al obtener los datos del producto: productoActual es null");
                            Toast.makeText(VentaActivity.this, "Error al obtener los datos del producto", Toast.LENGTH_SHORT).show();
                        }
                    } catch (DatabaseException e) {
                        Log.e(TAG, "Error al convertir los datos del producto: " + e.getMessage());
                        Toast.makeText(VentaActivity.this, "Error al convertir los datos del producto", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Producto no encontrado en la base de datos");
                    Toast.makeText(VentaActivity.this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error en la base de datos: " + error.getMessage());
                Toast.makeText(VentaActivity.this, "Error en la base de datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarProductoALaLista() {
        if (productoActual == null || TextUtils.isEmpty(codigoDeBarras)) {
            Toast.makeText(this, "Debe escanear un producto válido", Toast.LENGTH_LONG).show();
            return;
        }

        boolean productoExistente = false;
        for (Producto producto : listaProductos) {
            if (producto.getCodigoDeBarras().equals(productoActual.getCodigoDeBarras())) {
                int nuevaCantidad = producto.getCantidad() + 1;
                producto.setCantidad(nuevaCantidad);
                productoExistente = true;
                break;
            }
        }

        if (!productoExistente) {
            productoActual.setCantidad(1);
            listaProductos.add(productoActual);
        }

        adapter.notifyDataSetChanged();
        calcularTotalVenta();
    }

    private void calcularTotalVenta() {
        totalVenta = 0;
        for (Producto producto : listaProductos) {
            totalVenta += producto.getPrecio() * producto.getCantidad();
        }
        textViewTotal.setText("Total: S/ " + totalVenta);
    }

    private void registrarVenta() {
        if (listaProductos.isEmpty()) {
            Toast.makeText(this, "No hay productos para registrar la venta", Toast.LENGTH_LONG).show();
            return;
        }

        boolean haySuficienteStock = true;

        for (Producto producto : listaProductos) {
            if (producto.getCantidad() > producto.getStock()) {
                haySuficienteStock = false;
                Toast.makeText(this, "No hay suficiente stock para el producto: " + producto.getNombre(), Toast.LENGTH_LONG).show();
                break;
            }
        }

        if (haySuficienteStock) {
            String ventaId = mDatabase.child("Ventas").push().getKey();
            if (ventaId != null) {
                Venta venta = new Venta();
                venta.setId(ventaId);
                venta.setFechaVenta(getFechaActual());
                venta.setTotalVenta(totalVenta);

                for (Producto producto : listaProductos) {
                    int nuevaCantidad = producto.getStock() - producto.getCantidad();
                    producto.setStock(nuevaCantidad);

                    mDatabase.child("Productos").child(producto.getCodigoDeBarras()).setValue(producto)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Stock actualizado correctamente para el producto: " + producto.getCodigoDeBarras());
                            }).addOnFailureListener(e -> {
                                Toast.makeText(VentaActivity.this, "Error al actualizar el stock para el producto: " + producto.getCodigoDeBarras(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Error al actualizar el stock para el producto: " + producto.getCodigoDeBarras(), e);
                            });

                    venta.getProductos().add(producto);
                }

                mDatabase.child("Ventas").child(ventaId).setValue(venta)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(VentaActivity.this, "Venta registrada correctamente", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Venta registrada correctamente con ID: " + ventaId);
                            } else {
                                Toast.makeText(VentaActivity.this, "Error al registrar la venta", Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Error al registrar la venta", task.getException());
                            }
                        });

                listaProductos.clear();
                adapter.notifyDataSetChanged();
                textViewTotal.setText("Total: $0.0");
            }
        }
    }

    private void crearPdf() {
        // Obtener todas las ventas registradas
        mDatabase.child("Ventas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Venta> ventas = new ArrayList<>();

                    // Iterar sobre cada venta
                    for (DataSnapshot ventaSnapshot : dataSnapshot.getChildren()) {
                        Venta venta = ventaSnapshot.getValue(Venta.class);
                        if (venta != null) {
                            ventas.add(venta);
                        }
                    }

                    // Generar el PDF con todas las ventas obtenidas
                    if (!ventas.isEmpty()) {
                        generarPdf(ventas);
                    } else {
                        Toast.makeText(VentaActivity.this, "No hay ventas registradas", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VentaActivity.this, "No hay ventas registradas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al obtener las ventas", databaseError.toException());
                Toast.makeText(VentaActivity.this, "Error al obtener las ventas: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generarPdf(List<Venta> ventas) {
        Document document = new Document();
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/ReporteVentas.pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Title and metadata
            Paragraph title = new Paragraph("Reporte de Ventas");
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            // Table headers
            PdfPTable table = new PdfPTable(3);
            table.addCell("ID");
            table.addCell("Fecha de Venta");
            table.addCell("Total de Venta");

            // Populate table with sales data
            for (Venta venta : ventas) {
                table.addCell(venta.getId());
                table.addCell(venta.getFechaVenta());
                table.addCell(String.valueOf(venta.getTotalVenta()));
            }

            // Add table to document
            document.add(table);

            // Close document
            document.close();

            Toast.makeText(VentaActivity.this, "Reporte creado en: " + filePath, Toast.LENGTH_LONG).show();
        } catch (DocumentException | FileNotFoundException e) {
            Toast.makeText(VentaActivity.this, "Error al crear el PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error al crear el PDF: " + e.getMessage());
        }
    }

    private String getFechaActual() {
        // Obtener la fecha actual
        Date date = new Date();

        // Redondear al segundo más cercano
        long roundedTime = Math.round((double) date.getTime() / 1000) * 1000;

        // Crear el formato de fecha y aplicar la zona horaria adecuada
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        TimeZone timeZone = TimeZone.getTimeZone("GMT-5"); // Zona horaria de Lima, Peru (UTC-5)
        dateFormat.setTimeZone(timeZone);

        // Crear una nueva fecha con el tiempo redondeado
        Date roundedDate = new Date(roundedTime);

        // Formatear la fecha redondeada
        return dateFormat.format(roundedDate);
    }
}

