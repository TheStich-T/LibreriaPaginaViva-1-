package org.lpv.model;

import java.time.LocalDateTime;

public class Venta {

    private int idVenta;
    private double subtotal;
    private double descuento;
    private int usuarioAutorizaDescuento;
    private double total;
    private String estado;
    private long cuiCliente;
    private int idUsuario;
    private LocalDateTime fechaVenta;
    private LocalDateTime fechaAnulacion;
    private int usuarioAnulacion;
    private String motivoAnulacion;

    public Venta() {
    }

    public Venta(int idVenta, double subtotal, double descuento, int usuarioAutorizaDescuento, double total, String estado, long cuiCliente, int idUsuario, LocalDateTime fechaVenta, LocalDateTime fechaAnulacion, int usuarioAnulacion, String motivoAnulacion) {
        this.idVenta = idVenta;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.usuarioAutorizaDescuento = usuarioAutorizaDescuento;
        this.total = total;
        this.estado = estado;
        this.cuiCliente = cuiCliente;
        this.idUsuario = idUsuario;
        this.fechaVenta = fechaVenta;
        this.fechaAnulacion = fechaAnulacion;
        this.usuarioAnulacion = usuarioAnulacion;
        this.motivoAnulacion = motivoAnulacion;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public int getUsuarioAutorizaDescuento() {
        return usuarioAutorizaDescuento;
    }

    public void setUsuarioAutorizaDescuento(int usuarioAutorizaDescuento) {
        this.usuarioAutorizaDescuento = usuarioAutorizaDescuento;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getCuiCliente() {
        return cuiCliente;
    }

    public void setCuiCliente(long cuiCliente) {
        this.cuiCliente = cuiCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public int getUsuarioAnulacion() {
        return usuarioAnulacion;
    }

    public void setUsuarioAnulacion(int usuarioAnulacion) {
        this.usuarioAnulacion = usuarioAnulacion;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }
}

