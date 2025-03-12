package com.example.proyectofinal.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.example.proyectofinal.R;
import com.google.firebase.database.ValueEventListener;

public class GestionarUsuariosMainActivity extends AppCompatActivity {

    private EditText Edni, Enombre, Eapellido, Eusuario, Econtraseña;
    private Button registrar;
    private Toolbar toolbar;
    private ListView lvUsuarios;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> usuariosList;

    private String dni, nombre, apellido, usuario, rol, contraseña;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    private Spinner spinnerRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_usuarios_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.Ttoolbar);
        setSupportActionBar(toolbar);

        Edni = findViewById(R.id.editTextDNI);
        Enombre = findViewById(R.id.editTextNombres);
        Eapellido = findViewById(R.id.editTextApellidos);
        Eusuario = findViewById(R.id.editTextUsuario);
        Econtraseña = findViewById(R.id.editTextContraseña);

        lvUsuarios = findViewById(R.id.lv_usuarios);

        usuariosList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuariosList);
        lvUsuarios.setAdapter(adapter);

        spinnerRol = findViewById(R.id.spinnerRol);
        ArrayAdapter<CharSequence> rolAdapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(rolAdapter);

        obtenerUsuarios();
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
            dni = Edni.getText().toString();
            nombre = Enombre.getText().toString();
            apellido = Eapellido.getText().toString();
            usuario = Eusuario.getText().toString();
            rol = spinnerRol.getSelectedItem().toString();
            contraseña = Econtraseña.getText().toString();

            if (validarDatos(dni, nombre, apellido, usuario, rol, contraseña)) {
                registrarUsuario();
            }
            return true;
        } else if (id == R.id.icon_update) {
            dni = Edni.getText().toString();
            nombre = Enombre.getText().toString();
            apellido = Eapellido.getText().toString();
            usuario = Eusuario.getText().toString();
            rol = spinnerRol.getSelectedItem().toString();
            contraseña = Econtraseña.getText().toString();

            if (validarDatos(dni, nombre, apellido, usuario, rol, contraseña)) {
                actualizarUsuario(dni, nombre, apellido, usuario, rol, contraseña);
            }
            return true;
        } else if (id == R.id.icon_delete) {
            dni = Edni.getText().toString();
            if (!dni.isEmpty()) {
                eliminarUsuario(dni);
            } else {
                Toast.makeText(GestionarUsuariosMainActivity.this, "Debe proporcionar un DNI", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validarDatos(String dni, String nombre, String apellido, String usuario, String rol, String contraseña) {
        if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || rol.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(GestionarUsuariosMainActivity.this, "Debe completar los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dni.length() != 8 || !dni.matches("\\d+")) {
            Toast.makeText(GestionarUsuariosMainActivity.this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!nombre.matches("[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+")) {
            Toast.makeText(GestionarUsuariosMainActivity.this, "El nombre solo debe contener letras y espacios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!apellido.matches("[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+")) {
            Toast.makeText(GestionarUsuariosMainActivity.this, "El apellido solo debe contener letras y espacios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (contraseña.length() < 6) {
            Toast.makeText(GestionarUsuariosMainActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registrarUsuario() {
        mAuth.createUserWithEmailAndPassword(usuario, contraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", nombre);
                    map.put("apellido", apellido);
                    map.put("usuario", usuario);
                    map.put("rol", rol);
                    map.put("contraseña", contraseña);

                    mDatabase.child("Usuarios").child(dni).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if (task2.isSuccessful()) {
                                Toast.makeText(GestionarUsuariosMainActivity.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                                obtenerUsuarios();  // Actualizar la lista de usuarios
                            } else {
                                Toast.makeText(GestionarUsuariosMainActivity.this, "No se pudieron crear los datos correctamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(GestionarUsuariosMainActivity.this, "No se pudo registrar este usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void obtenerUsuarios() {
        mDatabase.child("Usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuariosList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String nombre = userSnapshot.child("nombre").getValue(String.class);
                    String apellido = userSnapshot.child("apellido").getValue(String.class);
                    String usuario = userSnapshot.child("usuario").getValue(String.class);
                    String rol = userSnapshot.child("rol").getValue(String.class);

                    String usuarioInfo = "Nombre: " + nombre + " " + apellido + "\nUsuario: " + usuario + "\nRol: " + rol;
                    usuariosList.add(usuarioInfo);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GestionarUsuariosMainActivity.this, "No se pudieron obtener los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUsuario(String dni, String nombre, String apellido, String usuario, String rol, String contraseña) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombre);
        map.put("apellido", apellido);
        map.put("usuario", usuario);
        map.put("rol", rol);
        map.put("contraseña", contraseña);

        mDatabase.child("Usuarios").child(dni).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(GestionarUsuariosMainActivity.this, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show();
                    obtenerUsuarios();  // Actualizar la lista de usuarios
                } else {
                    Toast.makeText(GestionarUsuariosMainActivity.this, "No se pudieron actualizar los datos correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void eliminarUsuario(String dni) {
        mDatabase.child("Usuarios").child(dni).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(GestionarUsuariosMainActivity.this, "Usuario eliminado con éxito", Toast.LENGTH_SHORT).show();
                    obtenerUsuarios();  // Actualizar la lista de usuarios
                } else {
                    Toast.makeText(GestionarUsuariosMainActivity.this, "No se pudo eliminar este usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}