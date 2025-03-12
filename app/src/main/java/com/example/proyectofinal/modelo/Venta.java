package com.example.proyectofinal.modelo;

import java.util.ArrayList;
import java.util.List;

public class Venta {
    private String id;
    private String fechaVenta;
    private double totalVenta;
    private List<Producto> productos;

    public Venta() {
        productos = new ArrayList<>(); // Inicialización de la lista en el constructor vacío
    }

    public Venta(String id, String fechaVenta, double totalVenta, List<Producto> productos) {
        this.id = id;
        this.fechaVenta = fechaVenta;
        this.totalVenta = totalVenta;
        this.productos = productos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(String fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
