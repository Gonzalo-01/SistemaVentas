package com.example.proyectofinal.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.proyectofinal.R;

public class OpcionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);
    }

    public void irRegistrar(View view){
        Intent i = new Intent(this, VentaActivity.class);
        startActivity(i);
    }

    public void irGestionarUsuariosMain(View view){
        Intent i=new Intent(this, GestionarUsuariosMainActivity.class);
        startActivity(i);

    }

    public void irActualizarInventario(View view){
        Intent i=new Intent(this,ActualizarInventarioActivity.class);
        startActivity(i);
    }
}