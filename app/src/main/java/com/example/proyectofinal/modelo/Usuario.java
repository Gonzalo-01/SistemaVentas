package com.example.proyectofinal.modelo;

public class Usuario {
    private int Dni;
    private String Nombres;
    private String Apellidos;
    private String Usuario;
    private String Rol;
    private String Contraseña;

    public Usuario() {
    }

    public Usuario(int dni, String nombres, String apellidos, String usuario, String rol, String contraseña) {
        Dni = dni;
        Nombres = nombres;
        Apellidos = apellidos;
        Usuario = usuario;
        Rol = rol;
        Contraseña = contraseña;
    }

    public int getDni() {
        return Dni;
    }

    public void setDni(int dni) {
        Dni = dni;
    }

    public String getNombres() {
        return Nombres;
    }

    public void setNombres(String nombres) {
        Nombres = nombres;
    }

    public String getApellidos() {
        return Apellidos;
    }

    public void setApellidos(String apellidos) {
        Apellidos = apellidos;
    }

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }

    public String getRol() {
        return Rol;
    }

    public void setRol(String rol) {
        Rol = rol;
    }

    public String getContraseña() {
        return Contraseña;
    }

    public void setContraseña(String contraseña) {
        Contraseña = contraseña;
    }
}
