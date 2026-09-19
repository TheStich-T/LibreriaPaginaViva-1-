package org.lpv.model;

import java.time.LocalDateTime;

public class MovimientoInventario {

    private int idMovimiento;
    private String isbn;
    private String titulo;
    private String tipoMovimiento;
    private int cantidad;
    private LocalDateTime fechaMovimiento;
    private int idUsuario;
    private String observacion;
    private String nitProveedor;

    public MovimientoInventario() {
    }

    public MovimientoInventario(String isbn, String tipoMovimiento, int cantidad, int idUsuario, String observacion, String nitProveedor) {
        this.isbn = isbn;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.idUsuario = idUsuario;
        this.observacion = observacion;
        this.nitProveedor = nitProveedor;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getNitProveedor() {
        return nitProveedor;
    }

    public void setNitProveedor(String nitProveedor) {
        this.nitProveedor = nitProveedor;
    }
}
